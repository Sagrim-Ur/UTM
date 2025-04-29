package com.example.unchaintaskmanager.ui.theme.task_list

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.example.unchaintaskmanager.util.UiEvent
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TaskListScreen(
    onNavigate: (UiEvent.Navigate) -> Unit,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val tasks = viewModel.tasks.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true) {

        viewModel.uiEvent.collect { event ->
            when(event)
            {
                is UiEvent.Navigate -> onNavigate(event)
                is UiEvent.ShowSnackBar -> {
                    val result = snackbarHostState.showSnackbar(event.message, actionLabel = event.action)
                    if(result == SnackbarResult.ActionPerformed){
                        viewModel.onEvent(TaskScreenEvent.OnUndoDeleteClick)
                    }
                }
                else -> Unit
            }
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.onEvent(TaskScreenEvent.OnAddTaskClick)
            },
                modifier = Modifier.padding(20.dp)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Фвв")

            }
        },
        floatingActionButtonPosition = FabPosition.End
    )
    {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks.value) { task ->
                TaskItem(task = task, onEvent = viewModel::onEvent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{
                            viewModel.onEvent(TaskScreenEvent.OnTaskClick(task))
                        }
                    )


            }
        }
    }


}