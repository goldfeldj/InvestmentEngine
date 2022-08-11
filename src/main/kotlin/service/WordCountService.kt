package service

import kotlinx.coroutines.launch
import words_engine.WordDatabase
import words_engine.WordRank
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class WordCountService(
    val wordDatabase: WordDatabase = WordDatabase()
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
