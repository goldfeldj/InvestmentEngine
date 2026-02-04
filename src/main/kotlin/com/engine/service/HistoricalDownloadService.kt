package com.engine.service

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.time.LocalDate

enum class DataType { TICKER, FOREX, METAL }

class HistoricalDownloadService(
    private val tickerDbPath: String = "tickers_history.db",
    private val forexDbPath: String = "forex_history.db"
) {

    /**
     * Entry point for Nasdaq Tickers (approx. 4,000 symbols)
     */
    fun syncNasdaqTickers(symbols: List<String>) {
        executeSync(tickerDbPath, symbols, DataType.TICKER)
    }

    /**
     * Entry point for Forex & Precious Metals (~60 pairs)
     */
    fun syncForexAndMetals() {
        val pairs = listOf("EUR/USD", "USD/JPY", "GBP/USD", "USD/ILS", "XAU/USD", "XAG/USD") // etc.
        executeSync(forexDbPath, pairs, DataType.FOREX)
    }

    private fun executeSync(dbPath: String, symbols: List<String>, type: DataType) {
        DriverManager.getConnection("jdbc:sqlite:$dbPath").use { conn ->
            applyBulkPragmas(conn)
            setupSchema(conn)

            symbols.forEach { symbol ->
                val lastDate = getLastDownloadedDate(conn, symbol) ?: LocalDate.now().minusYears(30)
                downloadInChunks(conn, symbol, lastDate)
            }
        }
    }

    private fun downloadInChunks(conn: Connection, symbol: String, startDate: LocalDate) {
        var currentStart = startDate.plusDays(1)
        val today = LocalDate.now()

        while (currentStart.isBefore(today)) {
            val chunkEnd = currentStart.plusYears(1).let { if (it.isAfter(today)) today else it }

            println("📡 [${symbol}] Fetching: $currentStart to $chunkEnd")

            // Mocking the data provider (e.g., Yahoo, Dukascopy, or CSV loader)
            val dataPoints = fetchFromProvider(symbol, currentStart, chunkEnd)

            if (dataPoints.isNotEmpty()) {
                saveBatch(conn, symbol, dataPoints)
                println("✅ [${symbol}] Saved up to $chunkEnd. Recovery checkpoint set.")
            }

            currentStart = chunkEnd.plusDays(1)
        }
    }

    private fun saveBatch(conn: Connection, symbol: String, rows: List<MarketRow>) {
        val sql = "INSERT OR IGNORE INTO market_data (symbol, date, open, close, volume) VALUES (?, ?, ?, ?, ?)"
        conn.autoCommit = false // Start Transaction
        try {
            conn.prepareStatement(sql).use { pstmt ->
                rows.chunked(1000).forEach { chunk ->
                    chunk.forEach { row ->
                        pstmt.setString(1, symbol)
                        pstmt.setString(2, row.date.toString())
                        pstmt.setDouble(3, row.open)
                        pstmt.setDouble(4, row.close)
                        pstmt.setLong(5, row.volume)
                        pstmt.addBatch()
                    }
                    pstmt.executeBatch()
                }
            }
            conn.commit()
        } catch (e: Exception) {
            conn.rollback()
            throw e
        }
    }

    private fun applyBulkPragmas(conn: Connection) {
        val statements = listOf(
            "PRAGMA journal_mode = WAL",
            "PRAGMA synchronous = NORMAL",
            "PRAGMA cache_size = -100000", // ~100MB cache
            "PRAGMA temp_store = MEMORY"
        )
        statements.forEach { conn.createStatement().execute(it) }
    }

    private fun setupSchema(conn: Connection) {
        conn.createStatement().execute("""
            CREATE TABLE IF NOT EXISTS market_data (
                symbol TEXT,
                date TEXT,
                open REAL,
                close REAL,
                volume INTEGER,
                PRIMARY KEY (symbol, date)
            )
        """)
    }

    private fun getLastDownloadedDate(conn: Connection, symbol: String): LocalDate? {
        val sql = "SELECT MAX(date) FROM market_data WHERE symbol = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, symbol)
            pstmt.executeQuery().use { rs ->
                return if (rs.next() && rs.getString(1) != null) LocalDate.parse(rs.getString(1)) else null
            }
        }
    }
}

data class MarketRow(val date: LocalDate, val open: Double, val close: Double, val volume: Long)
