package cyolo.cyolo.words_engine

import java.lang.IllegalArgumentException
import kotlin.math.sign

class WordCountComparator: Comparator<WordCount> {
    override fun compare(o1: WordCount?, o2: WordCount?): Int {
        if (o1 == null || o2 == null) {
            throw IllegalArgumentException("one or more of the arguments are null")
        }

        return (o1.count - o2.count).sign
    }
}
