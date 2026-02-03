package com.engine.service

import com.engine.model.OrchestrationConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class OrchestrationTest {

    private val parser = YamlParserService()
    private val configPath = "src/main/resources/orchestration.yaml"

    @Test
    fun `should parse all phases correctly from orchestration yaml`() {
        val config: OrchestrationConfig = parser.parseConfig(configPath)

        // Test Phase Existence
        assertTrue(config.phases.containsKey("macro_phase"))
        assertTrue(config.phases.containsKey("anti_thesis"))

        // Test snake_case to camelCase for research_depth
        val macro = config.phases["macro_phase"]!!
        assertEquals(8, macro.researchDepth)
        assertEquals("AI_MODERATED", macro.reconciliation)
    }

    @Test
    fun `should handle optional primary_model field`() {
        val config: OrchestrationConfig = parser.parseConfig(configPath)

        // Anti-thesis uses primary_model, not models list
        val antiThesis = config.phases["anti_thesis"]!!
        assertEquals("CLAUDE_3_5", antiThesis.models.first())
    }

    @Test
    fun `should handle missing reconciliation field in chairman phase`() {
        val config: OrchestrationConfig = parser.parseConfig(configPath)

        // Chairman phase does not have a reconciliation property in YAML
        val chairman = config.phases["chairman"]!!
        assertNull(chairman.reconciliation)
        assertEquals("GEMINI_3_FLASH", chairman.models.first())
    }

    @Test
    fun `edge case - should fail gracefully on non-existent file`() {
        assertThrows(java.io.FileNotFoundException::class.java) {
            parser.parseConfig<OrchestrationConfig>("non_existent.yaml")
        }
    }
}