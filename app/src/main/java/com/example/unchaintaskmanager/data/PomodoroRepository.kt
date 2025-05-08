package com.example.unchaintaskmanager.data

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

interface PomodoroRepository {

    /** Flow, на который сервис будет выкладывать тики, а ViewModel — подписываться */
    val timerFlow: SharedFlow<TimerTick>


    suspend fun insertSession(session: PomodoroSession): PomodoroSession

    suspend fun assignSessionToTask(pomodoroId: Int, linkedTaskId: Int)

    suspend fun getUnassignedSessions(): List<PomodoroSession>

    suspend fun getPomodoroSessionsForTask(linkedTaskId: Int): List<PomodoroSession>

    suspend fun updateSession(session: PomodoroSession)

    suspend fun getSessionById(id: Int): PomodoroSession?

    fun getSessionsForTask(taskId: Int): Flow<List<PomodoroSession>>

    fun publishTick(remainingMs: Long, isWorkPeriod: Boolean, sessionState: SessionState)

    //сохранение/загрузка состояния таймера
    suspend fun saveState(state: PomodoroState)

    suspend fun loadState(): PomodoroState?

    // --- НОВОЕ: финализировать сессию «одним махом» ---
    suspend fun completeSession(
        session: PomodoroSession,
        workDuration: Long,
        restDuration: Long
    )

    /** Очищает сохранённое live-состояние таймера из SharedPreferences */
    suspend fun clearSavedState()

}