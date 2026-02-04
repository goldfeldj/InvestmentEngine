package com.engine

import com.engine.service.ModelChainOrchestrator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import java.time.LocalDate

@SpringBootApplication(
    exclude = [
        org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration::class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration::class
    ]
)
class InvestmentEngineApplication(
    private val orchestrator: ModelChainOrchestrator
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        try {
            println("==========================================")
            println("   AI INVESTMENT ENGINE - WEEKLY AUDIT    ")
            println("==========================================")

            // 1. Define the 'Effective Date' (Usually Today)
            val today = LocalDate.now()

            // 2. Trigger the Orchestrator
            // This runs the full pipeline: YAML -> Macro -> Auditor -> Critic -> Chairman
            val result = orchestrator.runWeeklyAudit(today)

            // 3. Display Results
//            println("\n✅ AUDIT COMPLETE")
//            println("------------------------------------------")
//            println("Net Worth: ${result.totalNetWorthBaseCurrency}")
//            println("Risk Level: ${result.strategy.riskAssessment}")
//            println("Executive Dilemma: ${result.strategy.executiveDilemma}")
//            println("\n--- Recommendations ---")
//            result.strategy.finalRecommendations.forEach { rec ->
//                println(" > [${rec.actionType}] ${rec.assetTicker}: ${rec.rationale} (Urgency: ${rec.urgencyScore}/10)")
//            }

            // 4. Save Artifact (JSON Report)
            val mapper = jacksonObjectMapper().findAndRegisterModules()
            val outputFile = File("outputs/audit_report_${today}.json")
            outputFile.parentFile.mkdirs()
            mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, result)
            println("\n📄 Full report saved to: ${outputFile.absolutePath}")

        } catch (e: Exception) {
            System.err.println("❌ CRITICAL ENGINE FAILURE")
            e.printStackTrace()
        }
    }
}

fun main(args: Array<String>) {
    runApplication<InvestmentEngineApplication>(*args)
}
