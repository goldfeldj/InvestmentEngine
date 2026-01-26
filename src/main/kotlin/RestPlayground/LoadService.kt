package RestPlayground

import java.io.File

class LoadService {
    // The file is 5MB. Small (takes a few seconds). For a large file (hundreds of MBs), we'll parallelize (process file in chunks)
    fun loadValidWordBank(filePath: String): Set<String> { // ConcurrentSet?
        val file = File(filePath)
        if (!file.exists()) throw IllegalArgumentException("Word bank file not found at $filePath")

        // useLines prevents OOM errors
        // TODO: Think of bad input/edge cases
        return file.useLines { lines ->
            lines
                .map { it.trim() } // Remove surrounding whitespace only
                .filter { it.length >= 3 }
                // 'all { it in 'a'..'z' || it in 'A'..'Z' }' ensures strict English alphabetic (apply?)
                .filter { word -> word.all { char -> char.isLetter() } }
                .toSet()
        }
    }
}