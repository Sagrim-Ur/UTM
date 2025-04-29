package com.example.unchaintaskmanager.ui.theme.task_list

sealed class AddEditTaskEvent {
    data class OnTaskNameChange(val taskName: String): AddEditTaskEvent()
    data class OnTaskDescriptionChange(val taskDescription: String): AddEditTaskEvent()
    object OnSaveTaskClick: AddEditTaskEvent()
    object OnPomodoroLaunchClick: AddEditTaskEvent()
}