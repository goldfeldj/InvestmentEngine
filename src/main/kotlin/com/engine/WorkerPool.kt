package com.engine

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WorkerPool(numWorkers: Int) {

    private val executor: ExecutorService =
        Executors.newFixedThreadPool(numWorkers)

    private val freeWorkers: BlockingQueue<Worker> =
        LinkedBlockingQueue()

    init {
        repeat(numWorkers) { id ->
            val worker = Worker(id) {
                // callback when task finishes
                freeWorkers.put(worker)
            }
            freeWorkers.put(worker)
            executor.submit(worker)
        }
    }

    fun takeFreeWorker(): Worker {
        return freeWorkers.take() // blocks until a worker is free
    }

    fun shutdown() {
        executor.shutdownNow()
    }
}
