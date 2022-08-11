package cyolo.words_engine

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import kotlin.collections.HashMap
import kotlin.math.roundToLong

@Repository
class WordDatabase {
    private val database = HashMap<String, Long>()
    private val topFiveWordsQueue = PriorityQueue<WordCount>()
    private val operationQueue = ConcurrentLinkedQueue<Map<String, Long>>()

    private var isConsuming = false
    private val pendingHistogramRequestsSemaphore = AtomicInteger()
    private val lock = Mutex()

    private val rankFactor = 4.0
    private val histogramSize = 5

    // For a large input we can optimize by saving the top five word-counts when counting the new batch
    // That way we don't need to go over the entire <wordCounts> map, just over the top 5,
    // since at the worst all five of them will replace the existing values of <topFiveWordsQueue>
    private fun updateDatabases(wordCounts: Map<String, Long>?) {
        wordCounts?.forEach {
            val word = it.key
            val currentCount = database.get(word) ?: 0
            val newCount = it.value + currentCount
            database[word] = newCount

            if (topFiveWordsQueue.size < histogramSize) {
                topFiveWordsQueue.add(WordCount(word, newCount))
            } else if (newCount > topFiveWordsQueue.peek().count) {
                topFiveWordsQueue.remove()
                topFiveWordsQueue.add(WordCount(word, newCount))
            }
        }
    }

    suspend fun consume() {
        while (true) {
            lock.withLock {
                isConsuming = true
                while (operationQueue.isNotEmpty() && pendingHistogramRequestsSemaphore.get() == 0) {
                    val wordCounts = operationQueue.remove()
                    updateDatabases(wordCounts)
                }
                isConsuming = false
            }
        }
    }

    // Possible optimization: parallelize the below collect function (only justified for very large inputs...)
    fun produce(words: String) {
        val newWordsBatch = words.split(',')
        val wordCounts = newWordsBatch.stream().collect(Collectors.groupingBy({ e -> e }, Collectors.counting()))
        operationQueue.add(wordCounts)
    }


    // The sorting should be cheap for small constants, e.g five words.
    // If we switch to a large/dynamic number, a different strategy should be considered, e.g a sorted queue data-structure.
    private fun computeHistogram(): List<WordRank> {
        val topFiveWordsSortedSet = topFiveWordsQueue.toSortedSet(WordCountComparator())
        val leastOccurringWordCount = topFiveWordsSortedSet.last().count
        val mostOccurringWordCount = topFiveWordsSortedSet.first().count

        // Should <rankFactor> be changed to (<topFiveWordsSortedSet>.size - 1) when there are less than 5 words?
        return topFiveWordsSortedSet.map {
            WordRank(
                value = it.value,
                rank = ((rankFactor * (it.count - leastOccurringWordCount)) / (mostOccurringWordCount - 1)).roundToLong() + 1
            )
        }
    }

    // A possible optimization is to store a versioned histogram,
    // so that if several requests happen consecutively, only the first one will compute it
    suspend fun getHistogram(): List<WordRank> {
        pendingHistogramRequestsSemaphore.getAndIncrement()

        var histogram: List<WordRank>
        lock.withLock {
            histogram = computeHistogram()
        }

        pendingHistogramRequestsSemaphore.getAndDecrement()

        return histogram
    }
}
