//package assignments
//
//class Queue<T> {
//    class Node(
//        val prev: Node?,
//        val payload: T
//    )
//
//    var head: Node?
//    var tail: Node?
//
//    fun push(payload: T?) {
//        tail?.let {
//            tail.prev = Node(payload)
//            tail = tail.prev
//            return
//        }
//
//        tail = Node(payload)
//        head = tail
//    }
//
//    fun pull(): T? {
//        head ?: throw()
//
//        val returnValue = head
//        head = head.prev
//        if (head == null) {
//            tail = null
//        }
//
//        return returnValue.payload
//    }
//}
//
//// prefix = "" at root
//
//class StringPrinter() {
//    fun printPermutations(str: String) {
//        for (i in 0 ... str.length)
//        printPermutationsInner("", swap(str, 0, i))
//    }
//
//    fun printPermutationsInner(prefix: String, str: String) {
//        prefix += str[0]
//        str = str[1, str.length]
//        if (str.length == 0) {
//            println(prefix)
//        } else {
//            for (i in 0 ... str.length)
//            printPermutationsInner(prefix, swap(str, 0, i))
//        }
//    }
//}