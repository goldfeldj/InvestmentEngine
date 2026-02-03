package com.engine.service

import com.engine.model.*
import java.time.LocalDate

/**
 * Core domain service for executing the multi-phase AI Investment Audit.
 */
interface IntelligenceService {

    /**
     * Phase 1 (Consensus): Aggregates macro trends from multiple models.
     * Often used as the foundational "Macro Context" for subsequent audit phases.
     */
    fun getMacroConsensus(date: LocalDate, config: PhaseConfig?): String

    /**
     * Phase 2 (Orchestration): Executes the full Audit Chain.
     * Internally coordinates the Auditor, Anti-Thesis (Red Team), and Chairman.
     *
     * @param portfolio The global portfolio to be analyzed.
     * @param macroContext The macro narrative (usually from Phase 1).
     * @param date The strict date lock for data analysis.
     * @param config Configuration for model selection and compute depth per phase.
     * @return A list of validated, synthesized investment recommendations.
     */
    fun runAuditChain(
        portfolio: GlobalPortfolio,
        macroContext: String,
        date: LocalDate,
        config: OrchestrationConfig
    ): List<Recommendation>
}
