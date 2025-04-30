package com.example.unchaintaskmanager.ui.theme.pomodoro


sealed class PomodoroScreenEvent {
    object OnStartPomodoro: PomodoroScreenEvent()
    object OnPausePomodoro: PomodoroScreenEvent()
    object OnResumePomodoro: PomodoroScreenEvent()
    object OnRestartPomodoro: PomodoroScreenEvent()
    object onFinishPomodoro: PomodoroScreenEvent()

}