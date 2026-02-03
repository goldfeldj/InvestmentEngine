package com.engine.application

import com.engine.model.*
import com.engine.service.IntelligenceService
import com.engine.service.ModelDispatcher
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class IntelligenceServiceImpl(private val dispatcher: ModelDispatcher) : IntelligenceService {

    override fun getMacroConsensus(date: LocalDate, config: PhaseConfig?): String {
        val depth = config?.researchDepth ?: 5
        val prompt = """
            Analyze global macro trends for $date. 
            Focus on interest rates, inflation, and sector-specific shifts.
            Provide a high-level summary for an investment committee.
        """.trimIndent()

        // Phase 1 usually benefits from Gemini's massive context window
        return dispatcher.dispatch("GEMINI_PRO", prompt, depth)
    }

    override fun runAuditChain(
        portfolio: GlobalPortfolio,
        macro: String,
        date: LocalDate,
        config: OrchestrationConfig
    ): List<Recommendation> {

        // 1. PHASE: AUDITOR
        // Goal: Propose moves based on assets and macro context.
        val auditorOutput = getAuditorProposal(portfolio, macro, date, config.phases["audit_phase"])

        // 2. PHASE: ANTI-THESIS
        // Goal: Critique the auditor's proposal with a "devil's advocate" lens.
        val critique = getAntiThesis(auditorOutput, macro, date, config.phases["anti_thesis_phase"])

        // 3. PHASE: CHAIRMAN
        // Goal: Synthesize the proposal and the critique into final executable actions.
        return getChairmanDecision(auditorOutput, critique, date, config.phases["chairman_phase"]).finalRecommendations
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
    private fun getAuditorProposal(
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

    private fun getAntiThesis(
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

    private fun getChairmanDecision(
        auditorOutput: String,
        critique: String,
        date: LocalDate,
        config: PhaseConfig?
    ): StrategyReport {
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
        Your final decision must be a valid JSON object matching the StrategyReport schema:
        {
          "mainThesis": "Summary of the original auditor's proposal",
          "antiThesisSummary": "Summary of the key valid critiques found in the anti-thesis",
          "finalRecommendations": [
            {
              "assetTicker": "TICKER",
              "actionType": "BUY|SELL|HOLD|REBALANCE",
              "amount": 0.0,
              "currency": "USD",
              "urgencyScore": 1-10,
              "rationale": "Why this move survived the critique"
            }
          ],
          "riskAssessment": "LOW|MEDIUM|HIGH|CRITICAL",
          "executiveDilemma": "The primary trade-off or 'hard choice' the user must face"
        }
    """.trimIndent()

        val userPrompt = """
        Auditor's Proposal: $auditorOutput
        Anti-Thesis Critique: $critique
    """.trimIndent()

        val rawJson = dispatcher.dispatch(model, "$systemInstruction\n$userPrompt", depth)

        return parseStrategyReport(rawJson)
    }

    private val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private fun parseStrategyReport(rawResponse: String): StrategyReport {
        // 1. SANITIZE: Remove markdown blocks if the model included them
        val cleanJson = rawResponse
            .substringAfter("```json")
            .substringBeforeLast("```")
            .trim()

        return try {
            // 2. PARSE: Attempt to map to the data class
            mapper.readValue(cleanJson, StrategyReport::class.java)
        } catch (e: Exception) {
            // 3. FALLBACK: If sanitation failed, try parsing the raw string directly
            // in case there were no markdown backticks.
            try {
                mapper.readValue(rawResponse.trim(), StrategyReport::class.java)
            } catch (innerException: Exception) {
                // 4. CRITICAL FAILURE: Handle the "Broken JSON" dilemma
                throw RuntimeException("AI failed to return valid StrategyReport JSON: ${innerException.message}")
            }
        }
    }
}
