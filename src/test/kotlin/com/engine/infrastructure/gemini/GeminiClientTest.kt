package com.engine.infrastructure.gemini

import com.engine.infrastructure.configuration.AppConfig
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Disabled("heavy call to model; enable by demand")
@SpringBootTest(classes = [GeminiClient::class, AppConfig::class]) // Only load what you need
@ActiveProfiles("test")
class GeminiClientTest {
    @Autowired
    lateinit var geminiClient: GeminiClient

    @Test
    fun `verify gemini returns a response for a valid prompt`() {
        val response = geminiClient.prompt(
            modelName = "gemini-2.5-flash",
            prompt = "Hello",
            depth = 1
        )

        println("Gemini said: $response")

    }

    @Test
    fun `test Auditor logic with realistic portfolio state`() {
        // 1. Mock the "Digital Twin" YAML state
        val portfolioState = """
        asOfDate: 2026-02-04
        baseCurrency: USD
        assets:
          - ticker: AAPL
            type: EQUITY
            quantity: 150
            costBasis: 175.00
          - ticker: BTC
            type: DIGITAL_ASSET
            quantity: 0.5
            costBasis: 45000.00
          - ticker: CASH_ILS
            type: FIAT
            quantity: 50000
            exchangeRate: 3.0888
        constraints:
          riskTolerance: MODERATE
          taxJurisdiction: IL
    """.trimIndent()

        // 2. Build the Auditor Prompt
        val auditorPrompt = """
        STRICT DATE LOCK: Today is 2026-02-04.
        ROLE: Senior Portfolio Auditor. 
        TASK: Analyze the portfolio against the macro context.
    
        [COMPUTE ALLOCATION: 4/10]
        - Scale your analytical rigor linearly based on this 4/10 assignment. 
        - As depth increases, provide higher granularity in your Chain-of-Thought and cross-reference more market variables.
        - Efficiency: Conclude all processing within a 10-minute window, prioritizing insight density over sheer verbosity.
    
        Deep-dive into the assets and justify every move based only on data available on 2026-02-04.
        OUTPUT REQUIREMENTS:
        1. Executive Summary: 2-sentence 'State of the Union'.
        2. Asset Deep-Dive: Justify every 'Hold/Sell/Buy' with data from 2026-02-04. Per Sell/Buy Recommendation, suggest an actual portion from the holding. For the suggested buy/sell portion, express as [percentage of shares, number of shares, value of shares]
        3. Critical Vulnerabilities: Explicitly list 3 reasons this portfolio might fail.
        
        LIVE MARKET DATA:
        - AAPL: 269.48
        - BTC: 76194.50
        
        PORTFOLIO STATE:
        $portfolioState
    """.trimIndent()

        // 3. Execute with Depth 3 (Balanced compute/thinking)
        println("📡 Sending Audit Query to Gemini-2.5-Flash...")
        val response = geminiClient.prompt(
            modelName = "gemini-2.5-flash",
            prompt = auditorPrompt,
            depth = 3
        )

        println("✅ Auditor Response Received:\n$response")

        // 4. Assertions
        assert(response.isNotBlank())
    }
}
