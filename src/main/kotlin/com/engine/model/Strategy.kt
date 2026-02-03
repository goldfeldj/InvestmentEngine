package com.engine.model

import java.math.BigDecimal

/**
 * A specific action proposed by the Intelligence Service.
 */
data class Recommendation(
    val assetTicker: String,
    val actionType: ActionType,
    val amount: BigDecimal,
    val currency: String,
    val urgencyScore: Int, // 1-10
    val rationale: String
)

enum class ActionType { BUY, SELL, HOLD, REBALANCE }

/**
 * The final "Chairman" output.
 * Reconciles the primary thesis with the skeptic's anti-thesis.
 */
data class StrategyReport(
    val mainThesis: String,
    val antiThesisSummary: String,
    val finalRecommendations: List<Recommendation>,
    val riskAssessment: RiskLevel,
    val executiveDilemma: String // The "hard choice" presented to the user
)

enum class RiskLevel { LOW, MEDIUM, HIGH, CRITICAL }