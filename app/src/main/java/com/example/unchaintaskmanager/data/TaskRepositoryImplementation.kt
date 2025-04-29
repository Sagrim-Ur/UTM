package com.example.unchaintaskmanager.data

import kotlinx.coroutines.flow.Flow

class TaskRepositoryImplementation(
    private val dao: TaskDAO

): TaskRepository {

    override suspend fun insertTask(task: Task) {
        dao.insertTask(task)
    }

    override suspend fun deleteTask(task: Task) {
        dao.deleteTask(task)
    }

    override suspend fun getTaskById(id: Int): Task? {
        return dao.getTaskById(id)
    }

    override fun getTasks(): Flow<List<Task>> {
        return dao.getTasks()
    }
}