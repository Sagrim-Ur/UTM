package com.example.unchaintaskmanager.ui.theme.task_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unchaintaskmanager.data.Task
import com.example.unchaintaskmanager.data.TaskRepository
import com.example.unchaintaskmanager.util.Routes
import com.example.unchaintaskmanager.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository
): ViewModel() {

    val tasks = repository.getTasks()

    private val _uiEvent = Channel<UiEvent>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private var deletedTask: Task? = null

    fun onEvent(event: TaskScreenEvent){
        when(event){
            is TaskScreenEvent.OnTaskClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TASK + "?taskId=${event.task.id}"))
            }

            is TaskScreenEvent.OnAddTaskClick -> {
                sendUiEvent(UiEvent.Navigate(Routes.ADD_EDIT_TASK))

            }

            is TaskScreenEvent.DeleteTask -> {
                viewModelScope.launch {
                    deletedTask = event.task
                    repository.deleteTask(event.task)
                    sendUiEvent(
                        UiEvent.ShowSnackBar("Task Deleted", action = "Undo")
                    )
                }

            }
            is TaskScreenEvent.OnDoneChange -> {
                viewModelScope.launch {
                    repository.insertTask(event.task.copy(
                        isDone = event.isDone
                    ))

                }

            }
            is TaskScreenEvent.OnUndoDeleteClick -> {
                deletedTask?.let { task ->
                    viewModelScope.launch {
                        repository.insertTask(task)
                    }


                }


            }
            is TaskScreenEvent.OnPomodoroClickEvent -> {

                sendUiEvent(UiEvent.Navigate(Routes.POMODOROSCREEN + "?taskId=${event.task.id}"))

            }

        }


    }

    private fun sendUiEvent(event: UiEvent)
    {
        viewModelScope.launch {
            _uiEvent.send(event)

        }

    }
}