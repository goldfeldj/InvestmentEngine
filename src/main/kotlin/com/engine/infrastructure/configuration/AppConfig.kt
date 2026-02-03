package com.engine.infrastructure.configuration

import com.google.genai.Client
import com.openai.client.OpenAIClient
import com.openai.client.okhttp.OpenAIOkHttpClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
    @Value("\${gemini.api.key}")
    private lateinit var geminiApiKey: String

    @Value("\${openai.api.key}")
    private lateinit var openAiApiKey: String

    @Value("\${deepseek.api.key}")
    private lateinit var deepseekApiKey: String

    @Bean
    fun geminiSdkClient(): Client {
        return Client.builder()
            .apiKey(geminiApiKey)
            .build()
    }

    @Bean("openAiSdkClient")
    fun openAiSdkClient(): OpenAIClient {
        return OpenAIOkHttpClient.builder()
            .apiKey(openAiApiKey)
            .build()
    }

    @Bean("deepSeekSdkClient")
    fun deepSeekSdkClient(): OpenAIClient {
        return OpenAIOkHttpClient.builder()
            .baseUrl("https://api.deepseek.com")
            .apiKey(deepseekApiKey)
            .build()
    }
}
