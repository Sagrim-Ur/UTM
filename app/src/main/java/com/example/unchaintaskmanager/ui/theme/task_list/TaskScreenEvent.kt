package com.example.unchaintaskmanager.ui.theme.task_list

import com.example.unchaintaskmanager.data.Task

//Этот код определяет sealed class (запечатанный класс) TasksScreenEvent и его наследники.
// Sealed class (запечатанный класс) — это класс, который ограничивает список своих подклассов. Все возможные наследники должны быть определены внутри одного файла.
//Это позволяет удобно моделировать конечное множество событий или состояний.

sealed class TaskScreenEvent {
    data class DeleteTask(val task: Task): TaskScreenEvent()
    data class OnDoneChange(val task: Task, val isDone: Boolean): TaskScreenEvent()
    object OnUndoDeleteClick: TaskScreenEvent()
    data class OnTaskClick(val task: Task): TaskScreenEvent()
    object OnAddTaskClick: TaskScreenEvent()
    data class OnPomodoroClickEvent(val task: Task): TaskScreenEvent()
}