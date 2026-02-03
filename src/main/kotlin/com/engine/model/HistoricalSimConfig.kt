package com.engine.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class HistoricalSimConfig(
    @JsonProperty("simulation_setup")
    val setup: SimulationSetup,
    val initialAssets: GlobalPortfolio? = null,
    val benchmarks: List<String> = emptyList(),
    val logicOverrides: LogicOverrides = LogicOverrides()
)

data class SimulationSetup(
    val startDate: LocalDate,
    val timeFrame: Long, // in months
    val tickInterval: String? = null,
)

data class LogicOverrides(
    val applyTaxDrag: Boolean = true,
    val taxRate: Double = 0.25,
    val simulateSlippage: Double = 0.0
)