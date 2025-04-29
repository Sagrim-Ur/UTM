package com.example.unchaintaskmanager.ui.theme.task_list

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unchaintaskmanager.data.PomodoroRepository
import com.example.unchaintaskmanager.data.PomodoroSession
import com.example.unchaintaskmanager.data.Task
import com.example.unchaintaskmanager.data.TaskRepository
import com.example.unchaintaskmanager.util.Routes
import com.example.unchaintaskmanager.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val pomodoroRepository: PomodoroRepository,
    savedStateHandle: SavedStateHandle


): ViewModel() {
    var task by mutableStateOf<Task?>(null)
    private set

    var title by mutableStateOf("")
        private set

    var description by mutableStateOf("")
        private set

    private val _uiEvent = Channel<UiEvent>()

    val uiEvent = _uiEvent.receiveAsFlow()

    // 1) StateFlow для сеансов
    private val _pomodoroSessions =
        MutableStateFlow<List<PomodoroSession>>(emptyList())
    val pomodoroSessions: StateFlow<List<PomodoroSession>> =
        _pomodoroSessions

    init {
        val taskId = savedStateHandle.get<Int>("taskId")!!
        Log.d("ViewModel", "Полученный taskId: $taskId")  // 🔍 Проверка
        if(taskId != -1){
            viewModelScope.launch {
                repository.getTaskById(taskId)?.let { task ->
                    title = task.taskName
                    description = task.taskDescription ?: ""
                    this@AddEditTaskViewModel.task = task

                }
                pomodoroRepository.getSessionsForTask(taskId)
                    .collect { list ->
                        _pomodoroSessions.value = list
                    }

            }

        }
    }

    fun onEvent(event: AddEditTaskEvent){
        when(event)
        {
            AddEditTaskEvent.OnSaveTaskClick -> {
                viewModelScope.launch {
                    if(title.isBlank())
                    {
                        sendUiEvent(UiEvent.ShowSnackBar(
                            message = "The title can't be empty"
                        ))
                        return@launch
                    }
                    repository.insertTask(
                        Task(
                            taskName = title,
                            taskDescription = description,
                            isDone = task?.isDone ?: false,
                            id = task?.id

                        )
                    )
                    sendUiEvent(UiEvent.PopBackStack)
                }
            }
            is AddEditTaskEvent.OnTaskDescriptionChange -> {
                description = event.taskDescription
            }
            is AddEditTaskEvent.OnTaskNameChange -> {
                title = event.taskName
            }
            is AddEditTaskEvent.OnPomodoroLaunchClick ->
            {
                sendUiEvent(UiEvent.Navigate(Routes.POMODOROSCREEN + "?taskId=${task?.id}"))
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