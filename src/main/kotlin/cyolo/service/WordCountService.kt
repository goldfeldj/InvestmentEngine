package cyolo.cyolo.service

import kotlinx.coroutines.launch
import cyolo.cyolo.words_engine.WordDatabase
import cyolo.cyolo.words_engine.WordRank
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WordCountService(
    val wordDatabase: WordDatabase
) {
    init {
        runBlocking {
            launch {
                wordDatabase.consume()
            }
        }
    }
    fun postWords(words: String) {
        wordDatabase.produce(words)
    }

    fun getHistogram(): List<WordRank> {
        lateinit var histogram: List<WordRank>

        runBlocking {
            launch {
                histogram = wordDatabase.getHistogram()
            }
        }

        return histogram
    }
}
