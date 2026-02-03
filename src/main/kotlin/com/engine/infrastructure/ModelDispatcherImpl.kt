package com.engine.infrastructure

import com.engine.infrastructure.deepseek.DeepSeekClient
import com.engine.infrastructure.gemini.GeminiClient
import com.engine.infrastructure.openai.OpenAiClient
import com.engine.service.ModelDispatcher
import org.springframework.stereotype.Service

@Service
class ModelDispatcherImpl(
    private val geminiClient: GeminiClient,
    private val openAiClient: OpenAiClient,
    private val deepSeekClient: DeepSeekClient
) : ModelDispatcher {

    override fun dispatch(model: String, prompt: String, depth: Int): String {
        return when {
            model.startsWith("GEMINI") -> geminiClient.prompt(model, prompt, depth)
            model.startsWith("O1") || model.startsWith("GPT") -> openAiClient.prompt(model, prompt, depth)
            model.startsWith("DEEPSEEK") -> deepSeekClient.prompt(model, prompt, depth)
            else -> throw IllegalArgumentException("Unknown model: ${model}")
        }
    }
}
