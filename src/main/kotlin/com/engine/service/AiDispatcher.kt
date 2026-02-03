package com.engine.service

/**
 * Maps model names to their specific API implementations.
 * Includes mappings for active and alternative models.
 */
interface AiDispatcher {

    /**
     * Routes a prompt to the specified model and returns the raw response.
     * @param modelName The string ID from the YAML (e.g., "GEMINI_3_FLASH", "LLAMA_3_GROQ")
     */
    fun dispatch(modelName: String, prompt: String, depth: Int): String
}
