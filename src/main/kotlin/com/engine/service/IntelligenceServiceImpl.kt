package com.engine.service

import com.engine.model.GlobalPortfolio
import com.engine.model.OrchestrationConfig
import com.engine.model.PhaseConfig
import com.engine.model.Recommendation
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class IntelligenceServiceImpl(private val dispatcher: ModelDispatcher) : IntelligenceService {

    override fun runAuditChain(
        portfolio: GlobalPortfolio,
        macro: String,
        date: LocalDate,
        config: OrchestrationConfig
    ): List<Recommendation> {

        // 1. PHASE: AUDITOR
        // Goal: Propose moves based on assets and macro context.
        val auditorOutput = requestAuditorProposal(portfolio, macro, date, config.phases["audit_phase"])

        // 2. PHASE: ANTI-THESIS
        // Goal: Critique the auditor's proposal with a "devil's advocate" lens.
        val critique = requestAntiThesis(auditorOutput, macro, date, config.phases["anti_thesis_phase"])

        // 3. PHASE: CHAIRMAN
        // Goal: Synthesize the proposal and the critique into final executable actions.
        return requestChairmanDecision(auditorOutput, critique, date, config.phases["chairman_phase"])
    }

    companion object {
        // Default Model Constants
        private const val DEFAULT_MODEL_AUDITOR = "GEMINI_3_FLASH"
        private const val DEFAULT_MODEL_ANTI_THESIS = "GEMINI_3_FLASH"
        private const val DEFAULT_MODEL_CHAIRMAN = "GEMINI_1_5_PRO" // Prefer a smarter model for synthesis

        // Default Depth Constants
        private const val DEFAULT_DEPTH_AUDITOR = 5
        private const val DEFAULT_DEPTH_ANTI_THESIS = 3
        private const val DEFAULT_DEPTH_CHAIRMAN = 7
    }

    // ... private helper functions to format prompts and parse JSON responses ...
    private fun requestAuditorProposal(
        portfolio: GlobalPortfolio,
        macro: String,
        date: LocalDate,
        config: PhaseConfig?
    ): String {
        val model = config?.models?.firstOrNull() ?: DEFAULT_MODEL_AUDITOR
        val depth = config?.researchDepth ?: DEFAULT_DEPTH_AUDITOR

        val systemInstruction = """
        STRICT DATE LOCK: Today is $date.
    You are a Senior Portfolio Auditor. Analyze the provided portfolio and macro context.
    
    [COMPUTE ALLOCATION: $depth/10]
    - Scale your analytical rigor linearly based on this $depth/10 assignment. 
    - As depth increases, provide higher granularity in your Chain-of-Thought and cross-reference more market variables.
    - Efficiency: Conclude all processing within a 10-minute window, prioritizing insight density over sheer verbosity.
    
    Deep-dive into the assets and justify every move based only on data available on $date.
    """.trimIndent()

        val userPrompt = """
        Portfolio State: ${portfolio.toString()}
        Macro Context: $macro
    """.trimIndent()

        return dispatcher.dispatch(model, systemInstruction + "\n" + userPrompt, config?.researchDepth ?: 5)
    }

    private fun requestAntiThesis(
        auditorProposal: String,
        macro: String,
        date: LocalDate,
        config: PhaseConfig?
    ): String {
        val model = config?.models?.firstOrNull() ?: DEFAULT_MODEL_ANTI_THESIS
        val depth = config?.researchDepth ?: DEFAULT_DEPTH_ANTI_THESIS

        val systemInstruction = """
        STRICT DATE LOCK: Today is $date.
    You are a cynical Risk Strategist. Evaluate the following proposal to find flaws, risks, and counter-arguments.
    
    [COMPUTE ALLOCATION: $depth/10]
    - Scale your analytical rigor linearly: $depth/10. 
    - At higher depth, perform deeper adversarial stress-testing of the Auditor's assumptions.
    - Efficiency: Conclude all processing within a 10-minute window, prioritizing the severity and density of identified risks over sheer verbosity.
    """.trimIndent()

        val userPrompt = """
        Auditor's Proposal: $auditorProposal
        Macro Context: $macro
    """.trimIndent()

        return dispatcher.dispatch(model, "$systemInstruction\n$userPrompt", depth)
    }

    private fun requestChairmanDecision(
        auditorOutput: String,
        critique: String,
        date: LocalDate,
        config: PhaseConfig?
    ): List<Recommendation> {
        val model = config?.models?.firstOrNull() ?: DEFAULT_MODEL_CHAIRMAN
        val depth = config?.researchDepth ?: DEFAULT_DEPTH_CHAIRMAN

        val systemInstruction = """
        STRICT DATE LOCK: Today is $date.
        You are the Investment Chairman. You must adjudicate between the Auditor's Proposal and the Risk Strategist's Critique.
        
        [COMPUTE ALLOCATION: $depth/10]
        - Scale your analytical rigor linearly: $depth/10.
        - Higher depth requires a more nuanced reconciliation of conflicting data points between the two previous phases.
        - Efficiency: Conclude all processing within a 10-minute window.
        
        OUTPUT REQUIREMENT: 
        Your final decision must be a valid JSON array of Recommendation objects. 
        Each object must follow this structure:
        {
          "assetName": "Name",
          "action": "BUY|SELL|HOLD",
          "amount": 0.0,
          "reasoning": "Brief synthesis of why this move survived the critique."
        }
    """.trimIndent()

        val userPrompt = """
        Auditor's Proposal: $auditorOutput
        Anti-Thesis Critique: $critique
    """.trimIndent()

        val rawJson = dispatcher.dispatch(model, "$systemInstruction\n$userPrompt", depth)

        return parseRecommendations(rawJson)
    }
}
