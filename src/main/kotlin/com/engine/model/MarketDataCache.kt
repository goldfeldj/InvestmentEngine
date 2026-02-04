package com.engine.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "market_data_cache",
    uniqueConstraints = [UniqueConstraint(columnNames = ["ticker", "date", "type"])])
data class MarketDataCache(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val ticker: String,
    val date: LocalDate,
    val value: BigDecimal,

    @Enumerated(EnumType.STRING)
    val type: DataType, // PRICE or FX

    val provider: String,
    val updatedAt: LocalDate = LocalDate.now()
)

enum class DataType { PRICE, FX }