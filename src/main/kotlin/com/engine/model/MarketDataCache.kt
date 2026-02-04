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

    @Column(nullable = false, unique = true)
    val ticker: String,
    @Column(nullable = false)
    val date: LocalDate,
    @Column(nullable = false)
    val value: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: DataType, // PRICE or FX

    val provider: String,
    val updatedAt: LocalDate = LocalDate.now()
)

enum class DataType { PRICE, FX }
