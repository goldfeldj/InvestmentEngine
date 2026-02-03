package com.engine.service

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Provides real-time and historical pricing for all asset classes.
 */
interface MarketDataService {

    /**
     * Returns the price of a ticker at a specific date.
     * Uses current time if date is null.
     */
    fun getPrice(ticker: String, date: LocalDate? = null): BigDecimal

    /**
     * Fetches FX conversion rates (e.g., USD to ILS) for a specific date.
     */
    fun getExchangeRate(from: String, to: String, date: LocalDate? = null): BigDecimal
}
