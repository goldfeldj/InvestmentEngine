package assignments

class Cato {
    fun sumOfTwo(array: ArrayList<Int>, x: Int): Boolean {
        val valueSet = HashSet<Int>()

        array.forEach {
            valueSet.add(it) // TODO: Handle duplicate hash-values
        }

        array.forEach {
            val missingValue = x - it

            if (valueSet.contains(missingValue)) {
                return true
            }
        }

        return false
    }

    // 1,2,3
    // x = 6

    // [2,nil,3,nil,1]
}