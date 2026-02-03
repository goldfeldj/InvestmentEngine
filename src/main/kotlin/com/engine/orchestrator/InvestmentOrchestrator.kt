package com.engine.orchestrator

import com.engine.model.*
import java.time.LocalDate

/**
 * High-level coordinator for the intelligence pipeline.
 */
interface InvestmentOrchestrator {

    /**
     * Executes the standard weekly trigger flow.
     * Fetches current prices, runs AI consensus, and outputs recommendations.
     */
    fun runWeeklyAudit(portfolio: GlobalPortfolio): AuditResult

    /**
     * Executes a historical simulation from [startDate] for [months].
     * If [tickInterval] is null, performs a single-point audit.
     */
    fun runHistoricalSimulation(
        initialState: GlobalPortfolio,
        startDate: LocalDate,
        months: Long,
        tickInterval: TickInterval? = null
    ): SimulationSummary
}

enum class TickInterval { WEEKLY, MONTHLY }
