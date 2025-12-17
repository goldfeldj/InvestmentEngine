//package cyolo
//
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.boot.runApplication
//import org.springframework.web.bind.annotation.GetMapping
//import org.springframework.web.bind.annotation.PostMapping
//import org.springframework.web.bind.annotation.RequestBody
//import org.springframework.web.bind.annotation.RestController
//import cyolo.service.WordCountService
//import cyolo.words_engine.WordRank
//import org.springframework.beans.factory.annotation.Autowired
//import kotlin.concurrent.thread
//
//@SpringBootApplication
//class Application
//
//fun main(args: Array<String>) {
//	runApplication<Application>(*args)
//}
//
//@RestController
//class MessageResource {
//	@Autowired
//	lateinit var wordCountService: WordCountService
//
//	@GetMapping
//	fun getHistogram(): List<WordRank> =
//		wordCountService.getHistogram()
//
//	@PostMapping
//	fun post(@RequestBody payload: WordsPayload) {
//		thread {
//			wordCountService.postWords(payload.words)
//		}
//	}
//}
//
//data class WordsPayload(
//	val words: String
//)
