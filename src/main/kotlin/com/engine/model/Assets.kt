package com.engine.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * The root container for all financial data.
 * Designed to handle missing or empty sections gracefully.
 */
data class GlobalPortfolio(
    val metadata: PortfolioMetadata,
    val wallets: List<CashBalance> = emptyList(),
    val privatePortfolio: List<EquityAsset>? = null,
    val pensionFunds: List<PensionFund>? = null,
    val digitalAssets: List<DigitalAsset>? = null,
    val realEstate: List<RealEstateAsset>? = null,
    val humanCapital: List<IncomeAsset>? = null,
    val asOfDate: LocalDate? = null
)

data class PortfolioMetadata(
    val baseCurrency: String = "ILS",
    val defaultTickerCurrency: String = "USD"
)

data class CashBalance(
    val currency: String,
    val amount: BigDecimal
)

data class EquityAsset(
    val ticker: String,
    val shares: BigDecimal,
    val costBasis: BigDecimal,
    val currency: String? = null // Uses defaultTickerCurrency if null
)

data class PensionFund(
    val name: String,
    val currentValue: BigDecimal,
    val currentPathId: String,
    val possiblePaths: List<InvestmentPath>
)

data class InvestmentPath(
    val id: String,
    val structure: Map<String, Double>, // e.g., "foreign_stocks" to 1.0
    val proxyTicker: String? = null
)

data class DigitalAsset(
    val ticker: String,
    val amount: BigDecimal,
    val currency: String = "USD"
)

data class RealEstateAsset(
    val id: String,
    val currency: String,
    val estimatedValue: BigDecimal,
    val allowActions: Boolean = false
)

data class IncomeAsset(
    val id: String,
    val annualIncome: BigDecimal,
    val currency: String,
    val yearsToRetirement: Int,
    val industry: String
)
