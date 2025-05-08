package com.example.unchaintaskmanager.ui.theme.pomodoro

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.unchaintaskmanager.R
import com.example.unchaintaskmanager.data.SessionState
import com.example.unchaintaskmanager.util.UiEvent


@Composable
fun PomodoroScreen(onPopBackStack: () -> Unit,
                   viewModel: PomodoroViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val snackbarHostState = remember { SnackbarHostState() }

    val taskTitle by viewModel.taskTitle.collectAsState()

    //Забираем из модели состояние помидора
    //val timeLeft by viewModel.timeLeft // ← получаем текущее значение таймера
    //val isRunning = viewModel.isRunning
    //val isPaused = viewModel.isPaused
    //val isWork = viewModel.isWorkPeriod


    // Подписываемся на единый источник правды из ViewModel:
    val uiState by viewModel.uiState.collectAsState()

    val timeLeftMs   = uiState.remainingMs
    val isWork       = uiState.isWorkPeriod
    val sessionState = uiState.sessionState

    val isRunning    = sessionState == SessionState.RUNNING
    val isPaused     = sessionState == SessionState.PAUSED



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
    val totalSeconds = timeLeftMs / 1000
    val hours   = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60


    // Эффект: запуск помидора и обновление времени
    LaunchedEffect(Unit) {

        if (viewModel.pomodoroSession == null) {
            //viewModel.onEvent(PomodoroScreenEvent.OnStartPomodoro)
            viewModel.startPomodoro(context, viewModel.taskId)
        }
    }


    // Отображаем таймер на экране
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.steak),
            contentDescription = "Помидорка",
            modifier = Modifier
                .height(120.dp)
                .width(120.dp),
            contentScale = ContentScale.Crop
        )


        Text(
            text = if (isWork) taskTitle else "Перерыв",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопки управления таймером
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(onClick = {
                when {
                    isRunning -> {viewModel.pausePomodoro(context)
                        Log.d("PomodoroScreen", "кнопку паузы нажали")}
                    isPaused  -> {viewModel.resumePomodoro(context)
                        Log.d("PomodoroScreen", "кнопку продолжения нажали")}
                    else      -> {viewModel.startPomodoro(context, viewModel.taskId)
                        Log.d("PomodoroScreen", "кнопку паузы/продолжения/запуска нажали")}
                }
            }) {
                Text(if (isRunning) "Пауза" else if (isPaused) "Продолжить" else "Запустить")

            }

            Button(onClick = {
                viewModel.resetPomodoro(context)
            //    viewModel.startPomodoro(context, viewModel.taskId)
            }) {
                Text("Перезапуск")
            }

            Button(onClick = { viewModel.stopPomodoro(context)
            }) {
                Text("Завершить")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            Button(onClick = { viewModel.addTime(context, 5 * 60_000L)
            }) {
                Text("+ 5 минут")
            }

            Button(onClick = { viewModel.addTime(context, 15 * 60_000L) }) {
                Text("+15 минут")
            }

            Button(onClick = { viewModel.addTime(context, 30 * 60_000L) }) {
                Text("+30 минут")
            }
        }
    }


    }





