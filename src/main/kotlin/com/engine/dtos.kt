package com.engine

import java.time.Duration

data class AddTaskRequest(
    val type: TaskType,
    val message: String,
    val delay: Duration? = null,
    val period: Duration? = null
)

data class AddTaskResponse(
    val taskId: TaskId
)

data class TaskStatusResponse(
    val taskId: TaskId,
    val status: TaskStatus
)
