package com.engine.service

import com.engine.model.GlobalPortfolio
import com.engine.model.Recommendation
import org.springframework.stereotype.Service

@Service
class SimulationEngineImpl : SimulationEngine {
    override fun applyAction(action: Recommendation, state: GlobalPortfolio): GlobalPortfolio {
        // Return state as-is for now until you implement the math
        return state
    }

    override fun generateInsights(state: GlobalPortfolio): List<String> {
        return listOf("Insight: Portfolio risk is within normal bounds (Mock).")
    }
}
