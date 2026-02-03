package com.engine.service

interface ModelDispatcher {
    /**
     * Routes a prompt to the specified model and returns the response string.
     * @param model The model identifier (e.g., "GEMINI_1_5_PRO").
     * @param prompt The full system + user instruction.
     * @param depth The 1-10 scale for analytical rigor.
     */
    fun dispatch(model: String, prompt: String, depth: Int): String
}
