package com.miteshvasoya.focusflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miteshvasoya.focusflow.data.TaskEntity
import com.miteshvasoya.focusflow.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repo: TaskRepository) : ViewModel() {
    val tasks = repo.tasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun add(title: String) = viewModelScope.launch { if (title.isNotBlank()) repo.add(title) }
    fun toggle(task: TaskEntity) = viewModelScope.launch { repo.toggle(task) }
    fun remove(task: TaskEntity) = viewModelScope.launch { repo.remove(task) }
    fun clearCompleted() = viewModelScope.launch { repo.clearCompleted() }
}
