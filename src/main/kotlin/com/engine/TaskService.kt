package com.engine

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class TaskService(
    val runningTaskQueue: InMemoryConcurrentTaskQueue,
    val taskStore: InMemoryTaskStore
) {
    fun addTask(type: TaskType, message: String, delay: java.time.Duration?, period: java.time.Duration?): TaskId {
        val id = UUID.randomUUID()

        val task = Task(
            id = id,
            type = type,
            message = message,
            delay = delay,
            period = period,
        )

        taskStore.create(task)
        // TODO: Periodic cleanup of ONETIME and finished delayed tasks from store?
        if (type == TaskType.ONETIME) {
            val runningTask = RunningTask(id, TaskStatus.PENDING)
            runningTaskQueue.add(runningTask)
        }

        // TODO: schedule execution
        // Note: For delayed, we should remove remove once finished. Maybe lazily: mark a running instance has been created
        return id
    }

    fun getTaskStatus(taskId: TaskId): TaskStatus {
        return tasks[taskId]?.status
            ?: throw IllegalArgumentException("Task not found: $taskId")
    }

    fun updateTask(taskId: UUID, delay: java.time.Duration?, period: java.time.Duration?) {
        // TODO: Should ensure the store is thread safe
        // TODO: Check no running instances
        val task: Task = taskStore.get(taskId) ?: throw IllegalArgumentException("Task not found: $taskId")
        var newTask = task.copy()

        // TODO: Make the below and API more elegant
        if (task.type == TaskType.DELAYED) {
            newTask.delay = delay // TODO: This is wrong. We should compute how much time (save createdAt) had passed since its creation and subtract
        }
        else if (task.type == TaskType.PERIODIC) {
            newTask.period = period
        }
        else throw IllegalArgumentException("Task type is: ${task.type}; Must be DELAYED or PERIODIC"
        )

        // TODO: reschedule task
    }

    fun deleteTask(taskId: UUID) {
        taskStore.delete(taskId) // TODO: Check no running instances (or do we want to allow? How do we cancel mid run?)
            ?: throw IllegalArgumentException("Task not found: $taskId")
    }
}
