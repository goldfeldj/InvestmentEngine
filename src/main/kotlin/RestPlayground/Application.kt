// kotlin
package RestPlayground

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.ConcurrentHashMap
/*
Results from local run (took a few minutes):
{
  "the": 8049,
  "and": 4662,
  "The": 2716,
  "you": 2549,
  "that": 2022,
  "with": 1372,
  "our": 1327,
  "your": 914,
  "laptop": 868,
  "gaming": 848
}

 */

// TODO: Need to add tests
fun main() = runBlocking {
    // Change these to relative to project root
    val wordBankPath = "/Users/jonathangoldfeld/Dev/WordFrequency/src/main/resources/bank_of_words.txt"
    val urlsPath = "/Users/jonathangoldfeld/Dev/WordFrequency/src/main/resources/urls.txt"

    // Stage 1: Load
    val loadService = LoadService()
    val validWords = loadService.loadValidWordBank(wordBankPath)
    val globalCounts = ConcurrentHashMap<String, Int>()

    // Stage 2: Crawl
    val crawlingService = CrawlingService(validWords, globalCounts)
    val urls = File(urlsPath).readLines()
    crawlingService.crawlAll(urls)

    // Stage 3: Alert
    val alertService = AlertService(globalCounts)
    alertService.outputTop10()
}