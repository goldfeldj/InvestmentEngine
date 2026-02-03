package com.engine

import com.engine.orchestrator.ModelChainOrchestrator
import com.engine.service.YamlParserService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.LocalDate

@SpringBootApplication
class InvestmentEngineApplication(
    private val orchestrator: ModelChainOrchestrator,
    private val yamlParser: YamlParserService
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        println("🚀 Starting Investment Engine Weekly Audit...")

        // 1. Load your actual assets
        val portfolio = yamlParser.parsePortfolio("src/main/resources/assets.yaml")

        // 2. Execute the chain for 'today'
        val updatedPortfolio = orchestrator.execute(portfolio, LocalDate.now())

        // 3. Output/Save results
        println("✅ Audit Complete.")
        println("New Total Value: ${updatedPortfolio.wallets.sumOf { it.amount }}")

        // Optional: Save the state back to a result file
        yamlParser.savePortfolio(updatedPortfolio, "outputs/latest_audit_results.yaml")
    }
}

fun main(args: Array<String>) {
    runApplication<InvestmentEngineApplication>(*args)
}
