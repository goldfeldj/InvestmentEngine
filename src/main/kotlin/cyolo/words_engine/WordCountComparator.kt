package cyolo.words_engine

import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
import kotlin.math.sign

@Component
class WordCountComparator: Comparator<WordCount> {
    override fun compare(o1: WordCount?, o2: WordCount?): Int {
        if (o1 == null || o2 == null) {
            throw IllegalArgumentException("one or more of the arguments are null")
        }

        return if (o1.count - o2.count <= 0) -1
        else 1
    }
}
