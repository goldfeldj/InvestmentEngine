package com.engine.infrastructure.openai

import com.openai.client.OpenAIClient
import com.openai.models.ChatModel
import com.openai.models.ReasoningEffort
import com.openai.models.chat.completions.ChatCompletionCreateParams
import org.springframework.stereotype.Component

/**
 * Verified Adapter for OpenAI Java SDK 4.17.0+
 */
@Component
class OpenAiClient(private val client: OpenAIClient) {

    fun prompt(modelName: String, promptText: String, depth: Int): String {

        // Map 1-10 depth to categorical reasoning levels
        val effort = when {
            depth <= 3 -> ReasoningEffort.LOW
            depth <= 7 -> ReasoningEffort.MEDIUM
            else -> ReasoningEffort.HIGH
        }

        // Budget for both internal reasoning and the final answer
        val totalBudget = (depth * 4000).toLong()

        // Create the params using the fully qualified path
        val params = ChatCompletionCreateParams.builder()
            .model(ChatModel.of(modelName))
            .maxCompletionTokens(totalBudget)
            .reasoningEffort(effort)
            // developer role is preferred for 'System-level' instructions in 2026
            .addDeveloperMessage(promptText)
            .build()

        return try {
            val completion = client.chat().completions().create(params)

            // Extract text from the response choice (returns Optional<String>)
            completion.choices().firstOrNull()?.message()?.content()?.orElseThrow {
                IllegalStateException("OpenAI returned an empty response.")
            } ?: throw IllegalStateException("No choices found in OpenAI response.")

        } catch (e: Exception) {
            throw RuntimeException("OpenAI API Error (Depth $depth): ${e.message}", e)
        }
    }
}
