package com.engine

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

@Component
class InMemoryConcurrentTaskQueue {

    private val queue = ConcurrentLinkedQueue<RunningTask>()

    fun add(runningTask: RunningTask) {
        queue.add(runningTask)
    }

    fun get(taskId: UUID): RunningTask? =
        queue.remove()
}
