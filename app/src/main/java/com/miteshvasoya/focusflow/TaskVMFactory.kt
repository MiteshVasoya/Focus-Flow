package com.miteshvasoya.focusflow

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miteshvasoya.focusflow.data.TaskDatabase
import com.miteshvasoya.focusflow.data.TaskRepository

class TaskVMFactory(context: Context) : ViewModelProvider.Factory {
    private val repo = TaskRepository(TaskDatabase.get(context).taskDao())
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TaskViewModel(repo) as T
    }
}
