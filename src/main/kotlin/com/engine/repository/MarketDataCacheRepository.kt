package com.engine.repository

import com.engine.model.DataType
import com.engine.model.MarketDataCache
import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface MarketDataCacheRepository : CrudRepository<MarketDataCache, Long> {
    fun findByTickerAndDateAndType(ticker: String, date: LocalDate, type: DataType): MarketDataCache?
}