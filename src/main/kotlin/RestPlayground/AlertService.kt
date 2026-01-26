package RestPlayground

import java.util.concurrent.ConcurrentHashMap

class AlertService(private val globalCounts: ConcurrentHashMap<String, Int>) {

    // More efficient, need to verify:
    // 1. Define a comparator that compares Map entries by their value (frequency)
//    val comparator = compareBy<Map.Entry<String, Int>> { it.value }
//
//    // 2. Create a PriorityQueue that acts as a Min-Heap (size limited to 11)
//    val top10Heap = PriorityQueue(comparator)
//
//    for (entry in globalCounts.entries) {
//        if (entry.value <= 0) continue
//
//        top10Heap.add(entry)
//
//        // If we exceed 10, remove the smallest element (the root of the Min-Heap)
//        if (top10Heap.size > 10) {
//            top10Heap.poll()
//        }
//    }
//
//    // 3. Convert the heap back to a sorted map/list for the AlertService output
//    val top10 = top10Heap
//        .sortedByDescending { it.value }
//        .associate { it.key to it.value }

    fun outputTop10() {
        // 1. Get top 10 from the map
        val top10 = globalCounts.entries
            .filter { it.value > 0 } // Only include words that actually appeared
            .sortedByDescending { it.value } // Is there an efficient built-in get top-k function? (if not, is it worth it to write one? Seems so, since it will only take O(10*size) time complexity)
            .take(10)
            .associate { it.key to it.value }

        // 2. Format as Pretty JSON
        // Using a manual approach to keep it "Simple" without adding dependencies
        val json = top10.entries.joinToString(
            separator = ",\n",
            prefix = "{\n",
            postfix = "\n}"
        ) { "  \"${it.key}\": ${it.value}" }

        println(json)
    }
}