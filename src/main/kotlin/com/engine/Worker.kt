package com.engine

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class Worker(
    private val workerId: Int,
    private val onTaskFinished: () -> Unit
) : Runnable {

    private val inbox: BlockingQueue<RunningTask> = LinkedBlockingQueue()
    private val running = AtomicBoolean(true)

    fun assign(task: RunningTask) {
        inbox.put(task)
    }

    fun stop() {
        running.set(false)
    }

    override fun run() {
        while (running.get()) {
            try {
                val task = inbox.take() // wait for assignment
                execute(task)
                onTaskFinished()
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
        }
    }

    private fun execute(task: RunningTask) {
        println("Worker $workerId executing task ${task.taskId}")
        Thread.sleep(500) // simulate work
        println("Worker $workerId completed task ${task.taskId}")
    }
}
