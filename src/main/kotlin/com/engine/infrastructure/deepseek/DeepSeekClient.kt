package com.engine.infrastructure.deepseek

import com.openai.client.OpenAIClient
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionCreateParams
import org.springframework.stereotype.Component

/**
 * Adapter for DeepSeek (2026).
 * Since DeepSeek uses the OpenAI-compatible API, we reuse the OpenAI SDK classes
 * but configure this specific bean with the DeepSeek Base URL in AppConfig.
 */
@Component
class DeepSeekClient(private val deepSeekClient: OpenAIClient) {

    fun prompt(modelName: String, promptText: String, depth: Int): String {

        // DeepSeek doesn't use the 'reasoning_effort' parameter yet.
        // Instead, we use different model IDs:
        // "deepseek-chat" (V4) vs "deepseek-reasoner" (R1)
        val selectedModel = if (depth > 7) "deepseek-reasoner" else "deepseek-chat"

        val params = ChatCompletionCreateParams.builder()
            .model(ChatModel.of(selectedModel))
            .maxCompletionTokens(depth * 2000L)
            .addDeveloperMessage(promptText)
            .build()

        return try {
            val completion = deepSeekClient.chat().completions().create(params)
            completion.choices().firstOrNull()?.message()?.content()?.get()
                ?: throw IllegalStateException("DeepSeek returned an empty response.")
        } catch (e: Exception) {
            throw RuntimeException("DeepSeek API Error: ${e.message}", e)
        }
    }
}
