package com.engine.application

import com.engine.model.*
import com.engine.service.*
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class IntelligenceServiceImpl(
    private val dispatcher: ModelDispatcher,
    private val auditor: AuditorServiceImpl,
    private val antiThesis: AntiThesisServiceImpl,
    private val chairman: ChairmanServiceImpl
    ) : IntelligenceService {

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
        macroContext: String,
        date: LocalDate,
        config: OrchestrationConfig
    ): List<Recommendation> {

        // 1. PHASE: AUDITOR
        // Goal: Propose moves based on assets and macro context.
        val auditorOutput = auditor.getAuditorProposal(portfolio, macroContext, date, config.phases["audit_phase"])

        // 2. PHASE: ANTI-THESIS
        // Goal: Critique the auditor's proposal with a "devil's advocate" lens.
        val critique = antiThesis.getAntiThesis(auditorOutput, macroContext, date, config.phases["anti_thesis_phase"])

        // 3. PHASE: CHAIRMAN
        // Goal: Synthesize the proposal and the critique into final executable actions.
        return chairman.getChairmanDecision(auditorOutput, critique, date, config.phases["chairman_phase"]).finalRecommendations
    }
}
