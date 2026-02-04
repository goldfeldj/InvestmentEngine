package com.engine

import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryTaskStore {

    private val tasks = ConcurrentHashMap<UUID, Task>()

    fun create(task: Task) {
        tasks[task.id] = task
    }

    fun get(taskId: UUID): Task? =
        tasks[taskId]

    fun update(task: Task) {
        tasks[task.id] = task
    }

    fun delete(taskId: UUID) {
        tasks.remove(taskId)
    }

    fun exists(taskId: UUID): Boolean =
        tasks.containsKey(taskId)
}
