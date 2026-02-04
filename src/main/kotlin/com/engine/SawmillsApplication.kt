package com.engine

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class SawmillsApplication(
    val runningTaskQueue: InMemoryConcurrentTaskQueue,
) : CommandLineRunner {
    var numWorkers = 10

    override fun run(vararg args: String?) {
        val workerPool = WorkerPool(numWorkers = numWorkers)

        val orchestrator = WorkOrchestrator(runningTaskQueue, workerPool)
        val orchestratorThread = Thread(orchestrator, "orchestrator")
        orchestratorThread.start()

        // Submit tasks
        while (true) {
            val task = runningTaskQueue.take()          // wait for task
            val worker = workerPool.takeFreeWorker() // wait for worker
            worker.assign(task)
        }

        Thread.sleep(5000)

        orchestrator.stop()
        workerPool.shutdown()
    }
}

fun main(args: Array<String>) {
    runApplication<SawmillsApplication>(*args)
}

/*
Stores:
Tasks
RunningTasks
PendingTasks(Thread safe Queue)

APIs:
AddTask(type, delay, period) -> TaskId
GetTaskStatus(TaskId)
UpdateTask(delay/update/task itself?)
DeleteTask

Status:
[PENDING|RUNNING|FAILED|SUCCEEDED]

Type:
[ONETIME|DELAYED|PERIODIC]

Notes:
* One queue for all workers
  * One store for all (we need to check status, update, delete (check if running?))

  * one-time -> push to queue
  * delay/periodic -> polling thread on some store, then pushes to queue when time arrives

* ThreadPool(number -> machine resources, e.g. 10) (real life: scale)
ConcurrentTaskQueue
* Retries + backoff
* rate limits if calling external APIs, DBs, etc.
* coroutines to free up threads on IO etc. (if time allows)
* logging
* metrics
*/