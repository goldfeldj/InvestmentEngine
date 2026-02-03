package com.engine.service

import com.engine.model.PhaseConfig
import com.engine.model.StrategyReport
import java.time.LocalDate

interface ChairmanService {
    fun getChairmanDecision(
        auditorOutput: String,
        critique: String,
        date: LocalDate,
        config: PhaseConfig?
    ): StrategyReport
}