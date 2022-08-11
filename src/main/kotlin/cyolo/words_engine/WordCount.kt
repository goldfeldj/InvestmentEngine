package cyolo.words_engine

import org.springframework.beans.factory.annotation.Autowired

class WordCount(
    value: String,
    var count: Long = 0,
    private val comparator: WordCountComparator = WordCountComparator()
): Comparable<WordCount>, Word(value) {
    override fun compareTo(other: WordCount) =
        comparator.compare(this, other)
}
