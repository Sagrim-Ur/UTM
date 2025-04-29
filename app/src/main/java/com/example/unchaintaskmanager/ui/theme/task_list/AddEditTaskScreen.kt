package com.example.unchaintaskmanager.ui.theme.task_list

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unchaintaskmanager.ui.theme.pomodoro.PomodoroSessionItem
import com.example.unchaintaskmanager.util.Routes
import com.example.unchaintaskmanager.util.UiEvent
import com.google.firebase.perf.util.Timer

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddEditTaskScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    onPopBackStack: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {



    val pomodoroSessions by viewModel.pomodoroSessions.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is UiEvent.PopBackStack -> onPopBackStack()
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.ShowSnackBar -> {
                    val result = snackbarHostState.showSnackbar(event.message, actionLabel = event.action)
                }
                else -> Unit
                }
            }

        }


Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
        floatingActionButton = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.BottomEnd
            ) {
                Column(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(bottom = 16.dp, end = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.onEvent(AddEditTaskEvent.OnSaveTaskClick)
                        },

                        ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Сохранить задачу"
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            viewModel.onEvent(AddEditTaskEvent.OnPomodoroLaunchClick)
                            // Здесь твой второй обработчик (например, начать Pomodoro)
                        },

                        ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Помодоро"
                        ) // можно другой Icon
                    }
                }
            }
        }

) {
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            TextField(value = viewModel.title, onValueChange = {
                viewModel.onEvent(AddEditTaskEvent.OnTaskNameChange(it))

            },
                placeholder = {
                    Text(text = "Title")
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(value = viewModel.description, onValueChange = {
                viewModel.onEvent(AddEditTaskEvent.OnTaskDescriptionChange(it))

            },
                placeholder = {
                    Text(text = "Description")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 5
            )



    Column(modifier = Modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "История Pomodoro",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (pomodoroSessions.isEmpty()) {
            Text("Пока нет завершённых сеансов", modifier = Modifier.padding(8.dp))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // забирает оставшееся место
            ) {
                items(pomodoroSessions) { session ->
                    PomodoroSessionItem(session)
                    HorizontalDivider()
                }
            }
        }
    }
        }

}



}




