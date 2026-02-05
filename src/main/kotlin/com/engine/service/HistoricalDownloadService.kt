package com.engine.service

import org.springframework.stereotype.Service
import java.net.HttpURLConnection
import java.net.URL
import java.io.File
import java.io.PrintWriter
import java.util.Scanner
import java.sql.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Service
class HistoricalDownloadService(
    private val tickerDbPath: String = "./data/tickers_history.db",
    private val forexDbPath: String = "./data/forex_history.db",
    private val commoditiesDbPath: String = "./data/commodities_history.db"
) {
    // Tracking for summary report
    private val failedTickers = mutableMapOf<String, LocalDate>()
    private var totalProcessed = 0

    /**
     * Unified run function to perform a full system sync
     */
    fun performFullSystemSync() {
        // 1. Sync Tickers
        val nasdaqList = fetchNasdaqTickerList()
        if (nasdaqList.isNotEmpty()) {
            syncNasdaqTickers(nasdaqList)
        }

        // 2. Sync Forex & Precious Metals
        syncForex()
        syncCommodities()

        printSummaryReport()
    }

    /**
     * Scrapes the official Nasdaq Trader list.
     * Filter out Test Issues and Footer Metadata.
     */
    fun fetchNasdaqTickerList(): List<Pair<String, LocalDate>> {
        val cacheFile = File("src/main/resources/ticker_list.csv")

        // 1. Check if Cache exists
        if (cacheFile.exists()) {
            println("📦 Loading Nasdaq ticker list from cache: ${cacheFile.absolutePath}")
            return cacheFile.useLines { lines ->
                lines.mapNotNull { line ->
                    val parts = line.split(',')
                    if (parts.size == 2) {
                        parts[0] to LocalDate.parse(parts[1])
                    } else null
                }.toList()
            }
        }

        // 2. If no cache, download from Nasdaq
        val url = "https://www.nasdaqtrader.com/dynamic/symdir/nasdaqlisted.txt"
        val symbols = mutableListOf<String>()

        println("📡 Cache not found. Fetching Nasdaq master ticker list from web...")

        try {
            val connection = URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            Scanner(connection.getInputStream()).use { scanner ->
                if (scanner.hasNextLine()) scanner.nextLine() // Skip Header

                while (scanner.hasNextLine()) {
                    val line = scanner.nextLine()
                    if (line.startsWith("File Creation Time:")) break

                    val parts = line.split("|")
                    if (parts.size >= 4) {
                        val symbol = parts[0].trim()
                        val isTestIssue = parts[3].trim() == "Y"
                        if (symbol.isNotEmpty() && !isTestIssue) {
                            symbols.add(symbol)
                        }
                    }
                }
            }

            // 3. Write to Cache for next time
            if (symbols.isNotEmpty()) {
                println("💾 Enriching ${symbols.size} tickers with Inception Dates and writing to cache...")
                cacheFile.parentFile.mkdirs()
                PrintWriter(cacheFile).use { out ->
                    symbols.forEachIndexed { index, symbol ->
                        val firstTradeDate = getFirstTradeDate(symbol) ?: LocalDate.parse("1900-01-01")
                        out.println("$symbol,$firstTradeDate")

                        if ((index + 1) % 100 == 0) println("⏳ Processed ${index + 1}/${symbols.size} tickers...")
                    }
                }
            }

            println("✅ Successfully fetched and cached Nasdaq tickers.")
        } catch (e: Exception) {
            System.err.println("❌ Failed to fetch ticker list: ${e.message}")
        }

        // Return the newly cached data as Pairs
        return if (cacheFile.exists()) {
            cacheFile.readLines().map { line ->
                val parts = line.split(",")
                parts[0] to LocalDate.parse(parts[1])
            }
        } else emptyList()
    }

    private fun getFirstTradeDate(symbol: String): LocalDate {
        return try {
            // Request a tiny range just to get the 'meta' block
            val url = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?interval=1d&range=1d"
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "Mozilla/5.0")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().readText()
                val match = "\"firstTradeDate\":(\\d+)".toRegex().find(response)
                match?.groupValues?.get(1)?.toLongOrNull()?.let {
                    return Instant.ofEpochSecond(it).atZone(ZoneOffset.UTC).toLocalDate()
                }
            }
            LocalDate.now().minusYears(30) // Fallback if meta is missing
        } catch (e: Exception) {
            LocalDate.now().minusYears(30) // Fallback on connection error
        }
    }

    /**
     * Entry point: Synchronize Nasdaq Tickers.
     * Starts from the last saved date in DB or 30 years ago.
     */
    fun syncNasdaqTickers(symbols: List<Pair<String, LocalDate>>) {
        executeSync(tickerDbPath, symbols)
    }

    /**
     * Entry point: Synchronize Forex
     * Yahoo Finance uses specific suffixes for FX (e.g., "ILS=X", "XAUUSD=X").
     */
    fun syncForex() {
        val forexSymbols = listOf(
            "EURUSD=X", "USDJPY=X", "GBPUSD=X", "USDCHF=X", "AUDUSD=X", "USDCAD=X", "NZDUSD=X",
            "EURGBP=X", "EURJPY=X", "GBPJPY=X", "EURCHF=X", "EURCAD=X", "EURAUD=X", "GBPAUD=X",
            "CADJPY=X", "AUDJPY=X", "NZDJPY=X", "CHFJPY=X", "GBPCHF=X", "GBPCAD=X",
            "ILS=X", "USDHKD=X", "USDSGD=X", "USDMXN=X", "USDTRY=X",
            "USDCNY=X", "USDINR=X", "USDBRL=X", "USDZAR=X", "USDKRW=X"
        )

        println("🔍 Discovering Forex start dates...")
        val symbolsWithDates = forexSymbols.map { symbol ->
            val startDate = getFirstTradeDate(symbol)
            println("📍 $symbol: Available since $startDate")
            symbol to startDate
        }

        executeSync(forexDbPath, symbolsWithDates)
    }

    fun syncCommodities() {
        val commoditySymbols = listOf(
//            "GC=F", "SI=F",
//            "PL=F", "PA=F", "HG=F",
//            "CL=F", "BZ=F", "NG=F",
            "RB=F", "HO=F"
        )

        println("🔍 Discovering Commodity start dates...")
        val symbolsWithDates = commoditySymbols.map { symbol ->
            val startDate = getFirstTradeDate(symbol)
            println("📍 $symbol: Available since $startDate")
            symbol to startDate
        }

        executeSync(commoditiesDbPath, symbolsWithDates)
    }

    private fun executeSync(dbPath: String, symbols: List<Pair<String, LocalDate>>) {
        val file = File(dbPath)
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile() // Explicitly create it
            Thread.sleep(500)    // Give the OS/Indexer 500ms to "let go"
        }

        val url = "jdbc:sqlite:$dbPath?busy_timeout=10000"

        try {
            DriverManager.getConnection(url).use { conn ->
                // 3. Force WAL mode immediately.
                // WAL allows one writer and many readers, reducing "Busy" errors.
                conn.createStatement().use { stmt ->
                    stmt.queryTimeout = 10 // seconds
                    stmt.execute("PRAGMA journal_mode = WAL")
                }

                applyBulkPragmas(conn)
                setupSchema(conn)

                symbols.forEachIndexed { index, (symbol, firstTradeDate) ->
                    // Skip tickers where FirstTradeDate is the failure fallback
                    if (firstTradeDate == LocalDate.parse("1900-01-01")) {
                        println("⏭️ Skipping $symbol: Invalid inception date.")
                        return@forEachIndexed
                    }

                    val thirtyYearsAgo = LocalDate.now().minusYears(30)

                    // Start from the latest of (30 years ago, Inception Date)
                    val absoluteStart = if (firstTradeDate.isAfter(thirtyYearsAgo)) firstTradeDate else thirtyYearsAgo

                    // If DB already has data, start from the day after the last record; otherwise, use absoluteStart
                    val lastDateInDb = getLastDownloadedDate(conn, symbol)
                    val startPoint = lastDateInDb ?: absoluteStart

                    println("🔄 [${index + 1}/${symbols.size}] Processing $symbol starting from $startPoint...")
                    downloadInChunks(conn, symbol, startPoint)
                }
            }
        }
        catch (e: SQLException) {
            if (e.message?.contains("BUSY") == true) {
                println("❌ SQLite is still busy. Is another tool (like DB Browser) open?")
            }
            throw e
        }
    }

    private fun downloadInChunks(conn: Connection, symbol: String, startDate: LocalDate) {
        var currentStart = startDate.plusDays(1)
        val today = LocalDate.now()

        while (currentStart.isBefore(today)) {
            val chunkEnd = currentStart.plusYears(1).let { if (it.isAfter(today)) today else it }

            // Use our retry wrapper here
            val dataPoints = withRetry(symbol) { fetchYahooCsv(symbol, currentStart, chunkEnd) }

            if (!dataPoints.isNullOrEmpty()) {
                saveBatch(conn, symbol, dataPoints)
            } else if (dataPoints == null) {
                // Graceful exit for this specific chunk/ticker after 3 failures
                println("⏭️ Skipping chunk $currentStart for $symbol due to persistent errors.")
                failedTickers[symbol] = currentStart
                return
            }

            currentStart = chunkEnd.plusDays(1)
        }
    }

    private fun fetchYahooCsv(symbol: String, start: LocalDate, end: LocalDate): List<MarketRow> {
        val p1 = start.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
        val p2 = minOf(end.atStartOfDay(ZoneOffset.UTC).toEpochSecond(), LocalDate.now().minusDays(1).atStartOfDay(ZoneOffset.UTC).toEpochSecond())

        // Yahoo Finance v7 download endpoint
        val urlString = "https://query1.finance.yahoo.com/v8/finance/chart/$symbol?period1=$p1&period2=$p2&interval=1d"

        val connection = URL(urlString).openConnection() as HttpURLConnection
        // 1. Enhanced Headers
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")

        if (connection.responseCode != 200) throw Exception("HTTP ${connection.responseCode}: ${connection.responseMessage}")

        val rows = mutableListOf<MarketRow>()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val response = connection.inputStream.bufferedReader().readText()

            // Regex patterns to extract the arrays from the JSON structure
            val timestamps = "\"timestamp\":\\[(.*?)\\]".toRegex().find(response)?.groupValues?.get(1)?.split(",")
            val opens = "\"open\":\\[(.*?)\\]".toRegex().find(response)?.groupValues?.get(1)?.split(",")
            val closes = "\"close\":\\[(.*?)\\]".toRegex().find(response)?.groupValues?.get(1)?.split(",")
            val volumes = "\"volume\":\\[(.*?)\\]".toRegex().find(response)?.groupValues?.get(1)?.split(",")

            if (timestamps != null && opens != null && closes != null && volumes != null) {
                for (i in timestamps.indices) {
                    // v8 uses nulls for holidays/gaps; we skip them just like your CSV logic did
                    if (i < opens.size && i < closes.size && i < volumes.size &&
                        opens[i] != "null" && closes[i] != "null" && volumes[i] != "null") {

                        rows.add(MarketRow(
                            date = Instant.ofEpochSecond(timestamps[i].toLong())
                                .atZone(ZoneOffset.UTC).toLocalDate(),
                            open = opens[i].toDouble(),
                            close = closes[i].toDouble(),
                            volume = volumes[i].toLongOrNull() ?: 0L
                        ))
                    }
                }
            }
        }
        return rows
    }

    private fun saveBatch(conn: Connection, symbol: String, rows: List<MarketRow>) {
        val sql = "INSERT OR IGNORE INTO market_data (symbol, date, open, close, volume) VALUES (?, ?, ?, ?, ?)"
        conn.autoCommit = false
        try {
            conn.prepareStatement(sql).use { pstmt ->
                rows.forEach { row ->
                    pstmt.setString(1, symbol)
                    pstmt.setString(2, row.date.toString())
                    pstmt.setDouble(3, row.open)
                    pstmt.setDouble(4, row.close)
                    pstmt.setLong(5, row.volume)
                    pstmt.addBatch()
                }
                pstmt.executeBatch()
            }
            conn.commit()
        } catch (e: SQLException) {
            conn.rollback()
            throw e
        }
    }

    private fun applyBulkPragmas(conn: Connection) {
        val pragmas = listOf(
            "PRAGMA synchronous = NORMAL",
//            "PRAGMA synchronous = OFF", // Fastest for bulk imports
            "PRAGMA cache_size = -200000", // 200MB cache
            "PRAGMA temp_store = MEMORY"
        )
        conn.createStatement().use { stmt -> // One statement for all
            pragmas.forEach { sql ->
                stmt.execute(sql)
            }
        }
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
        // Optional: Run this after the INITIAL big sync for performance
        // conn.createStatement().execute("CREATE INDEX IF NOT EXISTS idx_symbol_date ON market_data(symbol, date)")
    }

    private fun getLastDownloadedDate(conn: Connection, symbol: String): LocalDate? {
        val sql = "SELECT MAX(date) FROM market_data WHERE symbol = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, symbol)
            pstmt.executeQuery().use { rs ->
                val dateStr = rs.getString(1)
                return if (dateStr != null) LocalDate.parse(dateStr) else null
            }
        }
    }

    private fun <T> withRetry(symbol: String, block: () -> T): T? {
        var delay = 2000L
        for (attempt in 1..3) {
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == 3) return null
                println("⚠️ [${symbol}] Attempt $attempt failed. Retrying in ${delay}ms...")
                Thread.sleep(delay)
                delay *= 2
            }
        }

        System.err.println("❌ Final attempt for symbol $symbol failed. Exiting gracefully.")
        return null
    }

    private fun printSummaryReport() {
        println("\n" + "=".repeat(40))
        println("📈 DOWNLOAD SUMMARY REPORT")
        println("=".repeat(40))
        println("Total Tickers Attempted: $totalProcessed")
        println("Successfully Synced:    ${totalProcessed - failedTickers.size}")
        println("Failed/Skipped:         ${failedTickers.size}")

        if (failedTickers.isNotEmpty()) {
            println("\n❌ FAILED TICKERS (RESUME POINTS):")
            println("%-10s | %-12s".format("Ticker", "Resume Date"))
            println("-".repeat(25))
            failedTickers.forEach { (ticker, date) ->
                println("%-10s | %-12s".format(ticker, date))
            }
        }
        println("=".repeat(40))
    }
}

data class MarketRow(val date: LocalDate, val open: Double, val close: Double, val volume: Long)
