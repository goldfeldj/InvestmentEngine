package com.engine.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * The result of a real-time weekly audit.
 */
data class AuditResult(
    val auditDate: LocalDate,
    val totalNetWorthBaseCurrency: BigDecimal,
    val strategy: StrategyReport,
    val insights: List<String> // Stress-tests, correlation warnings, etc.
)

/**
 * The high-level summary of a historical simulation run.
 */
data class SimulationSummary(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val initialNetWorth: BigDecimal,
    val finalNetWorth: BigDecimal,
    val totalTaxPaid: BigDecimal,
    val performanceVsBenchmark: Double, // Percentage difference
    val actionLog: List<SimulatedAction>
)

/**
 * A record of an action taken during a simulation "tick".
 */
data class SimulatedAction(
    val date: LocalDate,
    val recommendation: Recommendation,
    val executedPrice: BigDecimal,
    val taxImpact: BigDecimal
)
