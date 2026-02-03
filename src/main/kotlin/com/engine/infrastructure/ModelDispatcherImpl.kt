package com.engine.infrastructure

import com.engine.infrastructure.deepseek.DeepSeekClient
import com.engine.infrastructure.gemini.GeminiClient
import com.engine.infrastructure.openai.OpenAiClient
import com.engine.service.ModelDispatcher
import org.springframework.stereotype.Component

@Component
class ModelDispatcherImpl(
    private val geminiClient: GeminiClient,
    private val openAiClient: OpenAiClient,
    private val deepSeekClient: DeepSeekClient
) : ModelDispatcher {

    override fun dispatch(
        model: String,
        prompt: String,
        depth: Int,
        responseSchema: Class<*>?
    ): String {
        val upperModel = model.uppercase()

        return try {
            when {
                // Google Gemini 3 Series
                upperModel.contains("GEMINI") -> {
                    // Mapping logic: if user sends "GEMINI_FLASH", we send "gemini-3-flash" to the SDK
                    val actualModelId = mapToGeminiId(upperModel)
                    geminiClient.prompt(actualModelId, prompt, depth)
                }

                // OpenAI o-series and GPT-5
                upperModel.startsWith("O") || upperModel.contains("GPT") -> {
                    val actualModelId = mapToOpenAiId(upperModel)
                    openAiClient.prompt(actualModelId, prompt, depth, responseSchema)
                }

                // DeepSeek V4 / R1
                upperModel.contains("DEEPSEEK") -> {
                    val actualModelId = mapToDeepSeekId(upperModel)
                    deepSeekClient.prompt(actualModelId, prompt, depth)
                }

                else -> throw IllegalArgumentException("Unsupported model provider for: $model")
            }
        } catch (e: Exception) {
            // Log the failure and perhaps fallback to a default model
            throw RuntimeException("Dispatch failed for $model: ${e.message}", e)
        }
    }

    private fun mapToGeminiId(model: String) = when {
        model.contains("FLASH") -> "gemini-3-flash"
        else -> "gemini-3-pro"
    }

    private fun mapToOpenAiId(model: String) = when {
        model.startsWith("O") -> model.lowercase() // e.g., o1, o3
        else -> "gpt-5" // Default flagship
    }

    private fun mapToDeepSeekId(model: String) = when {
        model.contains("REASONER") || model.contains("R1") -> "deepseek-reasoner"
        else -> "deepseek-chat"
    }
}