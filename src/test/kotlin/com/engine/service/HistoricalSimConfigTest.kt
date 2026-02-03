package com.engine.service

import com.engine.model.HistoricalSimConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class HistoricalSimConfigTest {

    private val parser = YamlParserService()
    private val configPath = "src/main/resources/historical_sim_config.yaml"

    @Test
    fun `should parse simulation dates and frame correctly`() {
        val config: HistoricalSimConfig = parser.parseConfig(configPath)

        assertEquals(LocalDate.of(2023, 11, 1), config.setup.startDate)
        assertEquals(6L, config.setup.timeFrame)
    }

    @Test
    fun `should handle tick interval when provided`() {
        val config: HistoricalSimConfig = parser.parseConfig(configPath)

        // In our yaml it is "WEEKLY"
        assertEquals("WEEKLY", config.setup.tickInterval)
    }

    @Test
    fun `should parse nested logic overrides`() {
        val config: HistoricalSimConfig = parser.parseConfig(configPath)

        val overrides = config.logicOverrides
        assertTrue(overrides.applyTaxDrag)
        assertEquals(0.25, overrides.taxRate)
        assertEquals(0.001, overrides.simulateSlippage)
    }

    @Test
    fun `should parse benchmarks list`() {
        val config: HistoricalSimConfig = parser.parseConfig(configPath)

        assertTrue(config.benchmarks.contains("SPY"))
        assertEquals(3, config.benchmarks.size)
    }

    @Test
    fun `should parse embedded initial assets correctly`() {
        val config: HistoricalSimConfig = parser.parseConfig(configPath)
        val initialPortfolio = config.initialAssets
        assertNotNull(initialPortfolio)

        // Verify metadata inside the sim config
        assertEquals("ILS", initialPortfolio?.metadata?.baseCurrency)

        // Verify a specific asset
        val nvda = initialPortfolio?.privatePortfolio?.find { it.ticker == "NVDA" }
        // Wrap 10 in a BigDecimal to match the property type in the model
        assertEquals(java.math.BigDecimal("10"), nvda?.shares)
        assertEquals(java.math.BigDecimal("450.00"), nvda?.costBasis)
    }
}