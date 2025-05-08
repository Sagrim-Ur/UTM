package com.example.unchaintaskmanager.ui.theme.pomodoro

import com.example.unchaintaskmanager.data.SessionState

/**
 * Модель состояния экрана Pomodoro.
 * В ней храним всё, что нужно отрисовать в UI.
 */
data class PomodoroUiState(
    val remainingMs: Long = 25 * 60 * 1000L,
    val isWorkPeriod: Boolean = true,
    val sessionState: SessionState = SessionState.STOPPED,
    val taskTitle: String = "",          // опционально: чтобы сразу показать название задачи
)