package com.example.unchaintaskmanager.data

import kotlinx.coroutines.flow.Flow

//Это интерфейс, мы его

interface TaskRepository {

    suspend fun insertTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun getTaskById(id: Int): Task?

    fun getTasks(): Flow<List<Task>>

}