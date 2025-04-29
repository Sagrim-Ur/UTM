package com.example.unchaintaskmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: PomodoroSession): Long

    @Query("UPDATE PomodoroSession SET linkedTaskId = :linkedTaskId WHERE pomodoroId = :pomodoroId")
    suspend fun assignSessionToTask(pomodoroId: Int, linkedTaskId: Int)

    @Query("SELECT * FROM PomodoroSession WHERE linkedTaskId IS NULL")
    suspend fun getUnassignedSessions(): List<PomodoroSession>

    @Query("SELECT * FROM PomodoroSession WHERE linkedTaskId = :linkedTaskId ORDER BY startTime DESC")
    suspend fun getPomodoroSessionsForTask(linkedTaskId: Int): List<PomodoroSession>

    @Update
    suspend fun updateSession(session: PomodoroSession) //обновляем сессию

    @Query("SELECT * FROM PomodoroSession WHERE linkedTaskId = :taskId ORDER BY startTime DESC")
    fun getSessionsForTask(taskId: Int): Flow<List<PomodoroSession>>
}
