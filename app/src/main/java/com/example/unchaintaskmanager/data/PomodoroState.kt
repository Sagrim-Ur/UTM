package com.example.unchaintaskmanager.data

/**
 * Текущее «живое» состояние Pomodoro-таймера,
 * которое сохраняется и восстанавливается между запусками сервиса/приложения.
 */
data class PomodoroState(
    val remainingMs: Long,      // сколько миллисекунд осталось
    val isWorkPeriod: Boolean,  // true — рабочий интервал, false — перерыв
    val sessionState: SessionState
)