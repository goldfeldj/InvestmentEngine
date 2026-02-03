package com.engine.orchestrator

import com.engine.model.GlobalPortfolio
import com.engine.model.OrchestrationConfig
import com.engine.service.IntelligenceService
import com.engine.service.MarketDataService
import com.engine.service.SimulationEngine
import com.engine.service.YamlParserService
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class ModelChainOrchestrator(
    private val intelligence: IntelligenceService,
    private val simulation: SimulationEngine,
    private val yamlParser: YamlParserService,
    private val marketData: MarketDataService
) {
    fun execute(portfolio: GlobalPortfolio, effectiveDate: LocalDate): GlobalPortfolio {
        // 1. Context Injection (The "Amnesia" bit)
        // Pull the full orchestration config
        val config = yamlParser.parseConfig<OrchestrationConfig>("src/main/resources/orchestration.yaml")

// Extract the specific phase config for 'macro_phase'
        val macroPhaseConfig = config.phases["macro_phase"]

// Pass both to the intelligence service
        val macro = intelligence.getMacroConsensus(effectiveDate, macroPhaseConfig)

        // 2. The Chain
        val recommendations = intelligence.runAuditChain(portfolio, macro, effectiveDate, config)

        // 3. Execution (Market Impact)
        var updatedState = portfolio
        recommendations.forEach { rec ->
            updatedState = simulation.applyAction(rec, updatedState)
        }

        return updatedState.copy(asOfDate = effectiveDate)
    }
}
