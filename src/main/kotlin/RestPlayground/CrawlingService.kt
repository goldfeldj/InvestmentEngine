package RestPlayground

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jsoup.Jsoup

// FUTURE IMPROVEMENT: To handle per-host rate limiting (avoiding 429 errors),
// we could group URLs by domain and use a Mutex map + delay() to ensure
// we don't 'burst' a single server while still crawling different hosts in parallel.
class CrawlingService(
    private val wordBank: Set<String>,
    private val globalCounts: ConcurrentHashMap<String, Int>,
    private val maxConcurrentRequests: Int = 10 // TODO: fine-tune
) {
    private val client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build()

    // only maxConcurrentRequests urls are being actively downloaded at once.
    private val semaphore = Semaphore(maxConcurrentRequests)

    suspend fun crawlAll(urls: List<String>) = coroutineScope {
        urls.map { url ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    processUrl(url)
                }
            }
        }.awaitAll()
    }

    private fun processUrl(url: String) {
        try {
            val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())

            // TODO: Handle bad status codes
            if (response.statusCode() == 200) {
                // Split by non-alphabetic characters to isolate words
                // Note: response.body() loads the whole page into a String.
                // For most articles, this is fine. If we were crawling gigabyte-sized files, we would stream the response body instead.
                // Also, for such extremely large files (millions of words), it might be worth it to parallelize the work on each
                val document = Jsoup.parse(response.body()) // remove HTML tags
                val cleanText = document.text()
                val words = cleanText.split(Regex("[^a-zA-Z]+"))

                for (word in words) {
                    // Check the 3 rules + bank existence
                    if (wordBank.contains(word)) {
                        globalCounts.compute(word) { _, count -> (count ?: 0) + 1 }
                    }
                }
            }
        } catch (e: Exception) {
            System.err.println("Failed to fetch $url: ${e.message}")
        }
    }
}