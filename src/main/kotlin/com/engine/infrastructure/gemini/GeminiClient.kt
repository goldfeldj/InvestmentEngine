package com.engine.infrastructure.gemini

import com.google.genai.Client
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.ThinkingConfig
import org.springframework.stereotype.Component

/**
 * Adapter for the Google Gen AI SDK.
 * Note: ThinkingLevel is an inner enum of ThinkingConfig or a String constant
 * depending on your specific library version.
 */
@Component
class GeminiClient(private val client: Client) {

    fun prompt(modelName: String, prompt: String, depth: Int): String {

        // Linear Mapping: Depth 1 = 3200, Depth 10 = 32000
        // We ensure the value is at least 0.
        val calculatedBudget = (depth * 3200).coerceAtLeast(0)

        val thinkingConfig = ThinkingConfig.builder()
            .includeThoughts(false)
            // Use the calculated budget.
            // If depth was 0, it would effectively disable thinking.
            .thinkingBudget(calculatedBudget)
            .build()

        val config = GenerateContentConfig.builder()
            .maxOutputTokens(depth * 1000) // Your response token limit
            .temperature(1.0f - (depth * 0.08f))
            .thinkingConfig(thinkingConfig)
            .build()

        return try {
            val response = client.models.generateContent(modelName, prompt, config)
            response.text() ?: throw IllegalStateException("Gemini returned empty text.")
        } catch (e: Exception) {
            throw RuntimeException("Gemini API Error at depth $depth: ${e.message}", e)
        }
    }
}
