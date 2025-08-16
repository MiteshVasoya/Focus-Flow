package com.miteshvasoya.focusflow.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    val tasks: Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun add(title: String) {
        dao.insert(TaskEntity(title = title.trim()))
    }

    suspend fun toggle(task: TaskEntity) {
        dao.update(task.copy(done = !task.done))
    }

    suspend fun remove(task: TaskEntity) {
        dao.delete(task)
    }

    suspend fun clearCompleted() {
        dao.clearCompleted()
    }
}
