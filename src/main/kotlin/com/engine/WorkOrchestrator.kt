package com.engine

class WorkOrchestrator(
    private val queue: InMemoryConcurrentTaskQueue,
    private val workerPool: WorkerPool
) : Runnable {

    @Volatile
    private var running = true

    fun stop() {
        running = false
    }

    override fun run() {
        while (running) {
            try {
                // wait for task
                val task = queue.take()

                // wait for free worker
                val worker = workerPool.takeFreeWorker()

                // assign task
                worker.assign(task)

            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }
}
