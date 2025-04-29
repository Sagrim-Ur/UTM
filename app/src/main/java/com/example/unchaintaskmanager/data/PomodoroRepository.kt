package com.example.unchaintaskmanager.data

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

interface PomodoroRepository {

    suspend fun insertSession(session: PomodoroSession): PomodoroSession

    suspend fun assignSessionToTask(pomodoroId: Int, linkedTaskId: Int)

    suspend fun getUnassignedSessions(): List<PomodoroSession>

    suspend fun getPomodoroSessionsForTask(linkedTaskId: Int): List<PomodoroSession>

    suspend fun updateSession(session: PomodoroSession)

    fun getSessionsForTask(taskId: Int): Flow<List<PomodoroSession>>

}