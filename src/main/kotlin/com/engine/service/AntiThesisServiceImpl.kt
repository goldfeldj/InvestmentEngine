package com.engine.service

import com.engine.model.PhaseConfig
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class AntiThesisServiceImpl(private val dispatcher: ModelDispatcher): AntiThesisService {
    companion object {
        // Default Model Constants
        private const val DEFAULT_MODEL_ANTI_THESIS = "GEMINI_2.5_FLASH"
        private const val DEFAULT_DEPTH_ANTI_THESIS = 3
    }

    override fun getAntiThesis(
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
}