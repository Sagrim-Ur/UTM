package com.example.unchaintaskmanager.data

/**
 * Статус того, что сейчас происходит с таймером:
 * RUNNING — таймер идёт,
 * PAUSED  — таймер на паузе.
 */
enum class SessionState {
    RUNNING,
    PAUSED,
    STOPPED
}