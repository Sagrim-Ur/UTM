package com.example.unchaintaskmanager.ui.theme.pomodoro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.unchaintaskmanager.ui.theme.task_list.AddEditTaskViewModel
import com.example.unchaintaskmanager.ui.theme.task_list.TaskScreenEvent
import com.example.unchaintaskmanager.util.UiEvent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun PomodoroScreen(onPopBackStack: () -> Unit,
                   viewModel: PomodoroViewModel = hiltViewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }

    val taskTitle by viewModel.taskTitle.collectAsState()

    //Забираем из модели состояние помидора
    val timeLeft by viewModel.timeLeft // ← получаем текущее значение таймера
    val isRunning = viewModel.isRunning
    val isPaused = viewModel.isPaused
    val isWork = viewModel.isWorkPeriod


    val lifecycleOwner = LocalLifecycleOwner.current

    // Эффект: подписка на события
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.PopBackStack -> onPopBackStack()
                is UiEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.action
                    )
                }
                else -> Unit
            }
        }
    }

    // Конвертация миллисекунд в минуты и секунды
    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60


    // Эффект: запуск помидора и обновление времени
    LaunchedEffect(Unit) {

        if (viewModel.pomodoroSession == null) {
            viewModel.onEvent(PomodoroScreenEvent.OnStartPomodoro)
        }
    }


    // Отображаем таймер на экране
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isWork) taskTitle else "Перерыв",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки управления таймером
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                when {
                    isRunning -> {
                        viewModel.onEvent(PomodoroScreenEvent.OnPausePomodoro)
                    }

                    isPaused -> {
                        viewModel.onEvent(PomodoroScreenEvent.OnResumePomodoro)
                    }

                    else -> {
                        viewModel.onEvent(PomodoroScreenEvent.OnStartPomodoro)
                    }
                }
            }) {
                Text(if (isRunning) "Пауза" else if (isPaused) "Продолжить" else "Запустить")
            }

            Button(onClick = { viewModel.onEvent(PomodoroScreenEvent.OnRestartPomodoro) }) {
                Text("Перезапуск")
            }

            Button(onClick = { viewModel.onEvent(PomodoroScreenEvent.onFinishPomodoro) }) {
                Text("Завершить")
            }
        }
    }


    }





