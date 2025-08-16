package com.miteshvasoya.focusflow.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY done ASC, sortOrder DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TaskEntity): Long

    @Update
    suspend fun update(entity: TaskEntity)

    @Delete
    suspend fun delete(entity: TaskEntity)

    @Query("DELETE FROM tasks WHERE done = 1")
    suspend fun clearCompleted()
}
