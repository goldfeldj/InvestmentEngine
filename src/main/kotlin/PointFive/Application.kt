package PointFive

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json()
    }

    // Choose repository implementation here
    val repository: ItemRepository = InMemoryItemRepository()
    // For PostgreSQL, use:
    // val db = Database.connect("jdbc:postgresql://localhost:5432/yourdb", driver = "org.postgresql.Driver", user = "user", password = "pass")
    // val repository: ItemRepository = PostgresItemRepository(db)

    routing {
        route("/items") {
            get {
                call.respond(repository.getAll())
            }
            get("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@get
                }
                val item = repository.getById(id)
                if (item == null) {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                } else {
                    call.respond(item)
                }
            }
            post {
                val item = call.receive<Item>()
                val created = repository.add(item)
                call.respond(created)
            }
            put("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@put
                }
                val item = call.receive<Item>()
                val updated = repository.update(id, item)
                if (updated) {
                    call.respondText("Updated")
                } else {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }
            delete("{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("Invalid id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    return@delete
                }
                val deleted = repository.delete(id)
                if (deleted) {
                    call.respondText("Deleted")
                } else {
                    call.respondText("Not found", status = io.ktor.http.HttpStatusCode.NotFound)
                }
            }
        }
    }
}