package com.engine.infrastructure.openai

import com.github.victools.jsonschema.generator.OptionPreset
import com.github.victools.jsonschema.generator.SchemaGenerator
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder
import com.github.victools.jsonschema.generator.SchemaVersion
import com.github.victools.jsonschema.module.jackson.JacksonModule
import com.openai.client.OpenAIClient
import com.openai.core.JsonValue
import com.openai.models.ChatModel
import com.openai.models.ReasoningEffort
import com.openai.models.ResponseFormatJsonSchema
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionCreateParams.ResponseFormat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Verified Adapter for OpenAI Java SDK 4.17.0+
 */
@Component
class OpenAiClient(@Qualifier("openAiSdkClient") private val client: OpenAIClient) {

    fun prompt(modelName: String, promptText: String, depth: Int, schemaClass: Class<*>? = null): String {

        // Map 1-10 depth to categorical reasoning levels
        val effort = when {
            depth <= 3 -> ReasoningEffort.LOW
            depth <= 7 -> ReasoningEffort.MEDIUM
            else -> ReasoningEffort.HIGH
        }

        // Budget for both internal reasoning and the final answer
        val totalBudget = (depth * 4000).toLong()

        // Create the params using the fully qualified path
        val builder = ChatCompletionCreateParams.builder()
            .model(ChatModel.of(modelName))
            .maxCompletionTokens(totalBudget)
            .reasoningEffort(effort)
            // developer role is preferred for 'System-level' instructions in 2026
            .addDeveloperMessage(promptText)

        if (schemaClass != null) {
            val schemaString = generateSchema(schemaClass)

            val schema = ResponseFormatJsonSchema.JsonSchema.builder()
                .name(schemaClass.simpleName)
                .strict(true)
                .schema(JsonValue.from(schemaString)) // Pass as raw JsonValue
                .build()

            builder.responseFormat(
                ResponseFormat.ofJsonSchema(
                    ResponseFormatJsonSchema.builder()
                        .jsonSchema(schema)
                        .build()
                )
            )
        }

        return try {
            val completion = client.chat().completions().create(builder.build())

            // Extract text from the response choice (returns Optional<String>)
            completion.choices().firstOrNull()?.message()?.content()?.orElseThrow {
                IllegalStateException("OpenAI returned an empty response.")
            } ?: throw IllegalStateException("No choices found in OpenAI response.")

        } catch (e: Exception) {
            throw RuntimeException("OpenAI API Error (Depth $depth): ${e.message}", e)
        }
    }

    private fun generateSchema(clazz: Class<*>): String {
        val config = SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
            .with(JacksonModule())
            .build()
        val generator = SchemaGenerator(config)
        return generator.generateSchema(clazz).toString()
    }
}
