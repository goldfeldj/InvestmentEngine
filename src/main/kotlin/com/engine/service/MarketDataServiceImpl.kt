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

    private val fmpClient = RestClient.create("https://financialmodelingprep.com/api/v3")
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
        val response = fmpClient.get()
            .uri("/quote/$ticker?apikey=$fmpKey")
            .retrieve()
            .body(List::class.java) as List<Map<String, Any>>
        return BigDecimal(response[0]["price"].toString())
    }

    private fun fetchTiingoHistorical(ticker: String, date: LocalDate): BigDecimal {
        val response = tiingoClient.get()
            .uri("/daily/$ticker/prices?startDate=$date&endDate=$date")
            .header("Authorization", "Token $tiingoKey")
            .retrieve()
            .body(List::class.java) as List<Map<String, Any>>
        return BigDecimal(response[0]["adjClose"].toString())
    }

    private fun fetchFmpForex(from: String, to: String): BigDecimal {
        val response = fmpClient.get()
            .uri("/fx/${from}${to}?apikey=$fmpKey")
            .retrieve()
            .body(List::class.java) as List<Map<String, Any>>
        return BigDecimal(response[0]["bid"].toString())
    }
}