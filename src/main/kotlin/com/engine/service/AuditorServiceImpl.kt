package com.engine.service

import com.engine.model.GlobalPortfolio
import com.engine.model.PhaseConfig
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AuditorServiceImpl(private val dispatcher: ModelDispatcher): AuditorService {
    companion object {
        private const val DEFAULT_MODEL_AUDITOR = "GEMINI_2.5_FLASH"
        private const val DEFAULT_DEPTH_AUDITOR = 5
    }

    override fun getAuditorProposal(
        portfolio: GlobalPortfolio,
        macro: String,
        date: LocalDate,
        config: PhaseConfig?
    ): String {
        val model = config?.models?.firstOrNull() ?: DEFAULT_MODEL_AUDITOR
        val depth = config?.researchDepth ?: DEFAULT_DEPTH_AUDITOR

        // TODO: Add "LIVE MARKET DATA:" + assets' actual values
        val systemInstruction = """
        STRICT DATE LOCK: Today is $date.
        ROLE: Senior Portfolio Auditor. 
        TASK: Analyze the portfolio against the macro context.
    
        [COMPUTE ALLOCATION: $depth/10]
        - Scale your analytical rigor linearly based on this $depth/10 assignment. 
        - As depth increases, provide higher granularity in your Chain-of-Thought and cross-reference more market variables.
        - Efficiency: Conclude all processing within a 10-minute window, prioritizing insight density over sheer verbosity.
    
        Deep-dive into the assets and justify every move based only on data available on $date.
        OUTPUT REQUIREMENTS:
        1. Executive Summary: 2-sentence 'State of the Union'.
        2. Asset Deep-Dive: Justify every 'Hold/Sell/Buy' with data from $date. Per Sell/Buy Recommendation, suggest an actual portion from the holding. For the suggested buy/sell portion, express as [percentage of shares, number of shares, value of shares] 
        3. Critical Vulnerabilities: Explicitly list 3 reasons this portfolio might fail.
    """.trimIndent()

        val userPrompt = """
        Portfolio State: $portfolio
        Macro Context: $macro
    """.trimIndent()

        return dispatcher.dispatch(model, systemInstruction + "\n" + userPrompt, config?.researchDepth ?: depth)
    }
}