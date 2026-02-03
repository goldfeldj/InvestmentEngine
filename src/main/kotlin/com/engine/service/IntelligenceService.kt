package com.engine.service

import com.engine.model.*

/**
 * Interfaces with AI models to perform research and sentiment analysis.
 */
interface IntelligenceService {

    /**
     * Phase 1: Aggregates macro trends from multiple models (Moderated/Consensus).
     */
    fun getMacroConsensus(depth: Int): String

    /**
     * Phase 2: Audits the portfolio against the macro consensus.
     */
    fun auditPortfolio(portfolio: GlobalPortfolio, macroContext: String, depth: Int): List<Recommendation>

    /**
     * Phase 3: Critically challenges the audit results (Red Teaming).
     */
    fun generateAntiThesis(thesis: List<Recommendation>, depth: Int): String

    /**
     * Final Phase: Synthesizes Thesis and Anti-Thesis into the final Dilemma Report.
     */
    fun reconcileFinalStrategy(thesis: List<Recommendation>, antiThesis: String): StrategyReport
}