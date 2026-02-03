package com.engine.service

import com.engine.model.GlobalPortfolio
import com.engine.model.PhaseConfig
import java.time.LocalDate

interface AuditorService {
    fun getAuditorProposal(portfolio: GlobalPortfolio, macro: String, date: LocalDate, config: PhaseConfig?): String
}