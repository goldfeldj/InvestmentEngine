package com.engine.service

import com.engine.model.*
import java.math.BigDecimal

/**
 * Manages the "Digital Twin" portfolio, tracking cash and performance.
 */
interface SimulationEngine {

    /**
     * Updates the specific currency wallet and records capital gains taxes (25%).
     */
    fun applyAction(action: Recommendation, state: GlobalPortfolio): GlobalPortfolio

    /**
     * Calculates the correlation risk and stress test insights.
     */
    fun generateInsights(state: GlobalPortfolio): List<String>
}