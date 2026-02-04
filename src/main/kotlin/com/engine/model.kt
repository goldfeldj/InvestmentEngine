package com.engine

import java.time.Duration
import java.util.UUID

typealias TaskId = UUID

enum class TaskType {
    ONETIME,
    DELAYED,
    PERIODIC
}

enum class TaskStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    CANCELLED,
    FAILED
}

data class Task(
    val id: TaskId,
    val type: TaskType,
    val message: String,
    var delay: Duration?,
    var period: Duration?,
//    var status: TaskStatus
// TODO: A HashSet of RunningTask with
// TODO: Can a new Task instance start running if there are others not finished
)

data class RunningTask(
    val taskId: UUID,
    val status: TaskStatus
)

