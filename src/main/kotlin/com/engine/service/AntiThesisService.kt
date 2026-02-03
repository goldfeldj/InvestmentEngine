package com.engine.service

import com.engine.model.PhaseConfig
import java.time.LocalDate

interface AntiThesisService {
    fun getAntiThesis(auditorProposal: String, macro: String, date: LocalDate, config: PhaseConfig?): String
}