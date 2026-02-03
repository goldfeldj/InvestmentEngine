package com.engine.service

import com.engine.model.*
import java.time.LocalDate

/**
 * Interfaces with AI models to perform research and sentiment analysis.
 */
interface IntelligenceService {

    /**
     * Phase 1: Aggregates macro trends from multiple models (Moderated/Consensus).
     */
    fun getMacroConsensus(date: LocalDate, config: PhaseConfig?): String

    /**
     * Phase 2: Audits the portfolio against the macro consensus.
     */
    fun runAuditChain(
        portfolio: GlobalPortfolio,
        macroContext: String,
        date: LocalDate,
        config: OrchestrationConfig
    ): List<Recommendation>

    /**
     * Phase 3: Critically challenges the audit results (Red Teaming).
     */
    fun generateAntiThesis(thesis: List<Recommendation>, depth: Int): String

    /**
     * Final Phase: Synthesizes Thesis and Anti-Thesis into the final Dilemma Report.
     */
    fun reconcileFinalStrategy(thesis: List<Recommendation>, antiThesis: String): StrategyReport
}