package com.engine.service

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate

@Service
class MarketDataServiceImpl : MarketDataService {

    override fun getPrice(ticker: String, date: LocalDate?): BigDecimal {
        // For now, we return a mock price of 150.00 for everything
        // You can later integrate a real API here
        println("🔍 Mock Market Data: Fetching price for $ticker on ${date ?: "current date"}")
        return BigDecimal("150.00")
    }

    override fun getExchangeRate(from: String, to: String, date: LocalDate?): BigDecimal {
        // Defaulting to 1.0 (parity) or a common rate like USD/ILS = 3.7
        if (from == "USD" && to == "ILS") return BigDecimal("3.7")
        return BigDecimal("1.0")
    }
}
