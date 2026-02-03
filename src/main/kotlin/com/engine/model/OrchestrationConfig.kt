package com.engine.model

import com.fasterxml.jackson.annotation.JsonProperty

data class OrchestrationConfig(
    val phases: Map<String, PhaseConfig>
)

data class PhaseConfig(
    val description: String,
    val models: List<String> = emptyList(), // Handles single or multiple models
    val reconciliation: String? = null,
    @JsonProperty("research_depth")
    val researchDepth: Int
)
