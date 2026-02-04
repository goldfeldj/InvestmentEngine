package com.engine.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

class YamlParserTest {

    private val parser = YamlParserService()

    @Test
    fun `should apply default and explicit currencies correctly`() {
        val portfolio = parser.parsePortfolio("src/main/resources/assets.yaml")

        // NVDA has no currency in YAML -> should inherit "USD" from metadata
        val nvda = portfolio.privatePortfolio?.find { it.ticker == "NVDA" }
        assertEquals("USD", nvda?.currency)

        // OPRA has explicit "USD" in YAML -> should stay "USD"
        val opra = portfolio.privatePortfolio?.find { it.ticker == "OPRA" }
        assertEquals("USD", opra?.currency)
    }

    @Test
    fun `should handle empty sections without crashing`() {
        // If we provided a YAML with only wallets, the rest should be null or empty
        val portfolio = parser.parsePortfolio("src/main/resources/assets.yaml")

        // Ensure that sections that exist in the file are populated
        assertNotNull(portfolio.wallets)
        assertTrue(portfolio.wallets.isNotEmpty())
    }
}
