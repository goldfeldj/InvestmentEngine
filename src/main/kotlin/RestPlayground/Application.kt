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

// TODO: Need to add tests
fun main() = runBlocking {
    // Change these to relative to project root
    val wordBankPath = "/Users/jonathangoldfeld/Dev/MySandbox/src/main/resources/bank_of_words.txt"
    val urlsPath = "/Users/jonathangoldfeld/Dev/MySandbox/src/main/resources/urls.txt"

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

//fun main() {
////    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
//
//    val loadService = LoadService()
//    // In your main orchestrator:
//    val wordBank = loadService.loadValidWordBank("/path/to/bank.txt")
//
//// Initialize counts only for valid words to avoid growing the map with "junk" from the web
//    val globalWordCounts = ConcurrentHashMap<String, Int>().apply {
//        wordBank.forEach { put(it, 0) }
//    }
//}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    // Choose repository implementation here
    val repository: ItemRepository = InMemoryItemRepository()
    val dataAnalyzer: DataAnalyzer = DataAnalyzer()
    // For PostgreSQL, use:
    // val db = Database.connect("jdbc:postgresql://localhost:5432/yourdb", driver = "org.postgresql.Driver", user = "user", password = "pass")
    // val repository: ItemRepository = PostgresItemRepository(db)

    routing {
        route("/items") {
            get {
                call.respond(repository.getAll())
            }
            get("{id}") getById@{
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@getById
                }
                val item = repository.getById(id)
                if (item == null) {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                } else {
                    call.respond(item)
                }
            }
            get("total-value") {
                call.respond(dataAnalyzer.averageItemValue(repository))
            }
            post {
                val item = call.receive<Item>()
                val created = repository.add(item)
                call.respond(created)
            }
            put("{id}") putById@{
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@putById
                }
                val item = call.receive<Item>()
                val updated = repository.update(id, item)
                if (updated) {
                    call.respondText("Updated")
                } else {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }
            delete("{id}") deleteById@{
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@deleteById
                }
                val deleted = repository.delete(id)
                if (deleted) {
                    call.respondText("Deleted")
                } else {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }
            route("/by-name-substring") {
                get("{sub}") getBySubstring@{
                    val sub = call.parameters["sub"]
                    if (sub == null) {
                        call.respondText("Invalid substring", status = io.ktor.http.HttpStatusCode.BadRequest)
                        return@getBySubstring
                    }
                    val items = repository.getBySubstring(sub=sub)
                    if (items == null) {
                        call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                    } else {
                        call.respond(items)
                    }
                }
            }
        }
    }
}