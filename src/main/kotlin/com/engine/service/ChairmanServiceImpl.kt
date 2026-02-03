package com.engine.service

import com.engine.model.PhaseConfig
import com.engine.model.StrategyReport
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ChairmanServiceImpl(private val dispatcher: ModelDispatcher): ChairmanService {
    companion object {
        private const val DEFAULT_MODEL_CHAIRMAN = "GEMINI_1_5_PRO" // Prefer a smarter model for synthesis
        private const val DEFAULT_DEPTH_CHAIRMAN = 7
    }

    override fun getChairmanDecision(
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