package com.engine.service

import com.engine.model.DataType
import com.engine.model.MarketDataCache
import com.engine.repository.MarketDataCacheRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.math.BigDecimal
import java.time.LocalDate

@Service
class MarketDataServiceImpl(
    private val cacheRepo: MarketDataCacheRepository,
    @Value("\${fmp.api.key}") private val fmpKey: String,
    @Value("\${tiingo.api.key}") private val tiingoKey: String
) : MarketDataService {

    private val fmpClient = RestClient.create("https://financialmodelingprep.com/stable")
    private val tiingoClient = RestClient.create("https://api.tiingo.com/tiingo")

    override fun getPrice(ticker: String, date: LocalDate?): BigDecimal {
        val targetDate = date ?: LocalDate.now()

        // 1. Check SQLite Cache
        cacheRepo.findByTickerAndDateAndType(ticker, targetDate, DataType.PRICE)?.let {
            return it.value
        }

        // 2. Fetch from External API
        val price = if (targetDate == LocalDate.now()) {
            fetchFmpPrice(ticker) // FMP for current
        } else {
            fetchTiingoHistorical(ticker, targetDate) // Tiingo for historical
        }

        // 3. Save to Cache and Return
        cacheRepo.save(MarketDataCache(
            ticker = ticker, date = targetDate, value = price,
            type = DataType.PRICE, provider = if (targetDate == LocalDate.now()) "FMP" else "Tiingo"
        ))
        return price
    }

    override fun getExchangeRate(from: String, to: String, date: LocalDate?): BigDecimal {
        val pair = "${from}/${to}"
        val targetDate = date ?: LocalDate.now()

        cacheRepo.findByTickerAndDateAndType(pair, targetDate, DataType.FX)?.let {
            return it.value
        }

        val rate = fetchFmpForex(from, to) // FMP handles FX well

        cacheRepo.save(MarketDataCache(
            ticker = pair, date = targetDate, value = rate,
            type = DataType.FX, provider = "FMP"
        ))
        return rate
    }

    private fun fetchFmpPrice(ticker: String): BigDecimal {
        val data = getFirstRecord(fmpClient, "/quote-short?symbol=$ticker&apikey=$fmpKey")
        return data["price"]?.toString()?.toBigDecimal()
            ?: throw RuntimeException("No price field found for $ticker")
    }

    private fun fetchTiingoHistorical(ticker: String, date: LocalDate): BigDecimal {
        val data = getFirstRecord(tiingoClient, "/daily/$ticker/prices?startDate=$date&endDate=$date", tiingoKey)
        // Tiingo historical uses 'adjClose'
        return data["adjClose"]?.toString()?.toBigDecimal()
            ?: throw RuntimeException("No historical price found for $ticker on $date")
    }

    private fun fetchFmpForex(from: String, to: String): BigDecimal {
        val data = getFirstRecord(fmpClient, "/fx/$from$to?apikey=$fmpKey")
        return data["bid"]?.toString()?.toBigDecimal()
            ?: throw RuntimeException("No forex rate found for $from$to")
    }

    /**
     * Helper to safely extract the first record from a list-based API response.
     */
    private fun getFirstRecord(client: RestClient, uri: String, token: String? = null): Map<String, Any> {
        val request = client.get().uri(uri)
        if (token != null) request.header("Authorization", "Token $token")

        val response = request.retrieve().body(List::class.java) as? List<*>

        if (response.isNullOrEmpty()) {
            throw RuntimeException("API returned no data for URI: $uri")
        }

        @Suppress("UNCHECKED_CAST")
        return response[0] as? Map<String, Any>
            ?: throw RuntimeException("API response format error at URI: $uri")
    }
}
