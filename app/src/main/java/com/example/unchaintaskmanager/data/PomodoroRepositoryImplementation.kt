package com.example.unchaintaskmanager.data

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

//
class PomodoroRepositoryImplementation @Inject constructor(
    private val dao: PomodoroDAO,
    private val prefs: SharedPreferences

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

    override suspend fun getSessionById(id: Int): PomodoroSession? =
        dao.getSessionById(id)

    override fun getSessionsForTask(taskId: Int): Flow<List<PomodoroSession>>
    {
        return dao.getSessionsForTask(taskId)
    }

    // 1) Внутренний flow для публикации тиков
    private val _timerFlow = MutableSharedFlow<TimerTick>(replay = 1)

    // 2) Публичный SharedFlow для подписчиков
    override val timerFlow: SharedFlow<TimerTick> = _timerFlow.asSharedFlow()

    // 3) Метод, который будет вызываться из PomodoroService
    override fun publishTick(remainingMs: Long, isWorkPeriod: Boolean, sessionState: SessionState) {
        // tryEmit — не блокирует и сразу отдаёт значение, если возможно
        _timerFlow.tryEmit(TimerTick(remainingMs, isWorkPeriod, sessionState))
    }

    // 2) Сохранение live-состояния
    override suspend fun saveState(state: PomodoroState)
    {
        prefs.edit()
            .putLong("KEY_REMAINING", state.remainingMs)
            .putBoolean("KEY_IS_WORK", state.isWorkPeriod)
            .putString("KEY_STATE", state.sessionState.name)
            .apply()
    }

    override suspend fun loadState(): PomodoroState? {
        if (!prefs.contains("KEY_STATE")) return null
        return PomodoroState(
            remainingMs  = prefs.getLong("KEY_REMAINING", 0L),
            isWorkPeriod = prefs.getBoolean("KEY_IS_WORK", true),
            sessionState = SessionState.valueOf(prefs.getString("KEY_STATE", SessionState.PAUSED.name)!!)
        )
    }

    override suspend fun completeSession(
        session: PomodoroSession,
        workDuration: Long,
        restDuration: Long
    ) {
        // Копируем исходный объект, добавляем поля endTime, workDuration, restDuration
        val updated = session.copy(
            endTime      = System.currentTimeMillis(),
            workDuration = workDuration,
            restDuration = restDuration
        )
        // Отдаём всё DAO на обновление
        dao.updateSession(updated)
    }

    override suspend fun clearSavedState() {
        prefs.edit().clear().apply()
    }


}