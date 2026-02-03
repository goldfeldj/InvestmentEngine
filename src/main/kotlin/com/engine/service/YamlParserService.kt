package com.engine.service

import com.engine.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import org.springframework.stereotype.Service

@Service
class YamlParserService {

    val mapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
        setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE)
        // Ensure we don't write nulls to the YAML to keep it clean
        setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
        // Important: Disable writing dates as timestamps so we get "2026-02-03"
        configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    /**
     * Parses the portfolio and applies global defaults for currency.
     */
    fun parsePortfolio(filePath: String): GlobalPortfolio {
        val file = File(filePath)
        if (!file.exists()) throw IllegalArgumentException("Portfolio file not found at $filePath")

        val rawPortfolio: GlobalPortfolio = mapper.readValue(file)

        // Use the base_currency from metadata if specific asset currency is missing
        val defaultCurrency = rawPortfolio.metadata.defaultTickerCurrency

        return rawPortfolio.copy(
            privatePortfolio = rawPortfolio.privatePortfolio?.map {
                it.copy(currency = it.currency ?: defaultCurrency)
            },
            digitalAssets = rawPortfolio.digitalAssets?.map {
                it.copy(currency = if (it.currency.isEmpty()) defaultCurrency else it.currency)
            }
        )
    }

    /**
     * Parses simulation or orchestration configs.
     */
    final inline fun <reified T> parseConfig(filePath: String): T {
        val file = File(filePath)
        return mapper.readValue(file)
    }

    fun savePortfolio(portfolio: GlobalPortfolio, destinationPath: String) {
        val file = File(destinationPath)
        file.parentFile?.mkdirs() // Create directories if they don't exist
        mapper.writeValue(file, portfolio)
    }
}
