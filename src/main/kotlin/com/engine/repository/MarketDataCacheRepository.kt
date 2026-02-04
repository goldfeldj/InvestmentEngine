package com.engine.repository

import com.engine.model.DataType
import com.engine.model.MarketDataCache
import org.springframework.data.jpa.repository.JpaRepository // Import JpaRepository
import java.time.LocalDate

// Change CrudRepository to JpaRepository
interface MarketDataCacheRepository : JpaRepository<MarketDataCache, Long> {
    fun findByTickerAndDateAndType(ticker: String, date: LocalDate, type: DataType): MarketDataCache?
}
