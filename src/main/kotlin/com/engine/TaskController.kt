package com.engine

import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun addTask(@RequestBody request: AddTaskRequest): AddTaskResponse {
        val taskId = taskService.addTask(
            type = request.type,
            message = request.message,
            delay = request.delay,
            period = request.period
        )
        return AddTaskResponse(taskId)
    }

    @GetMapping("/{taskId}/status")
    fun getTaskStatus(@PathVariable taskId: UUID): TaskStatusResponse {
        val status = taskService.getTaskStatus(taskId)
        return TaskStatusResponse(taskId, status)
    }

    @PutMapping("/{taskId}")
    fun updateTask(
        @PathVariable taskId: UUID,
        @RequestBody request: AddTaskRequest
    ) {
        taskService.updateTask(
            taskId,
            delay = request.delay, // Delay further if hasn't started (check task type)
            period = request.period // Change period (check task type)
        )
    }

    @DeleteMapping("/{taskId}")
    fun deleteTask(@PathVariable taskId: UUID) {
        taskService.deleteTask(taskId)
    }
}
