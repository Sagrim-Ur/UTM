package com.example.unchaintaskmanager.data

import kotlinx.coroutines.flow.Flow

//
class PomodoroRepositoryImplementation(
    private val dao: PomodoroDAO

): PomodoroRepository
{

    override suspend fun insertSession(session: PomodoroSession): PomodoroSession
    {
        val id = dao.insertSession(session)
        return session.copy(pomodoroId = id.toInt())
    }

    override suspend fun assignSessionToTask(pomodoroId: Int, linkedTaskId: Int)
    {
        dao.assignSessionToTask(pomodoroId, linkedTaskId)
    }

    override suspend fun getUnassignedSessions(): List<PomodoroSession>
    {
        return dao.getUnassignedSessions()
    }

    override suspend fun getPomodoroSessionsForTask(linkedTaskId: Int): List<PomodoroSession>
    {
        return dao.getPomodoroSessionsForTask(linkedTaskId)
    }

    override suspend fun updateSession(session: PomodoroSession) {
        dao.updateSession(session) // 🔹 просто проксируем DAO
    }

    override fun getSessionsForTask(taskId: Int): Flow<List<PomodoroSession>>
    {
        return dao.getSessionsForTask(taskId)
    }

}