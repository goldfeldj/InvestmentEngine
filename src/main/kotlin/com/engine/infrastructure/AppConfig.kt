package com.engine.infrastructure

import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {

    @Bean("openAiSdkClient")
    fun openAiClient(@Value("\${openai.api.key}") apiKey: String): OpenAIClient {
        return OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            // Default is api.openai.com
            .build()
    }

    @Bean("deepSeekSdkClient")
    fun deepSeekClient(@Value("\${deepseek.api.key}") apiKey: String): OpenAIClient {
        return OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            // Redirect to DeepSeek's OpenAI-compatible endpoint
            .baseUrl("https://api.deepseek.com")
            .build()
    }
}