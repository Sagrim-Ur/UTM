package com.example.unchaintaskmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.unchaintaskmanager.ui.theme.UnchainTaskManagerTheme
import com.example.unchaintaskmanager.ui.theme.pomodoro.PomodoroScreen
import com.example.unchaintaskmanager.ui.theme.pomodoro.PomodoroScreenEvent
import com.example.unchaintaskmanager.ui.theme.task_list.AddEditTaskScreen
import com.example.unchaintaskmanager.ui.theme.task_list.TaskListScreen
import com.example.unchaintaskmanager.util.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UnchainTaskManagerTheme {

                    AppNavigation()

                }
            }
        }
    }

@Composable
fun AppNavigation()
{
    val navController = rememberNavController()

    Scaffold (
        //Это наш бар
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly // Распределяет элементы равномерно
                    ) {
                        IconButton(onClick = { /*navController.navigate("Analytics") */}) {
                            Icon(Icons.Filled.Build, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { navController.navigate(Routes.TASK_SCREEN) }) {
                            Icon(
                                Icons.Filled.DateRange,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = { navController.navigate(Routes.OVERVIEWSCREEN)}) {
                            Icon(Icons.Filled.Home, contentDescription = "Localized description")
                        }
                        IconButton(onClick = { /* navController.navigate("Tasks Swiper") */}) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Localized description"
                            )
                        }
                        IconButton(onClick = { /* navController.navigate("Goals") */}) {
                            Icon(Icons.Filled.Star, contentDescription = "Localized description")
                        }
                    }
                }
            ) /*{ Text("Bottom Bar") }*/
        }
    )

    { paddingValues -> // `paddingValues` передаётся в content

        NavHost(navController = navController, startDestination = Routes.TASK_SCREEN, modifier = Modifier.padding(paddingValues)){ // Учитываем отступ){


            composable(Routes.TASK_SCREEN) {
                TaskListScreen(onNavigate = {
                    navController.navigate(it.route)
                })
            }
            composable(route = Routes.ADD_EDIT_TASK + "?taskId={taskId}",
                arguments = listOf(
                    navArgument(name = "taskId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )

            )

            {
                AddEditTaskScreen(onPopBackStack = {
                    navController.popBackStack()
                },
                    onNavigate = {
                        navController.navigate(it.route)
                    })

            }

            composable(Routes.OVERVIEWSCREEN) {
                OverviewScreen (onNavigateToSecondScreen = { navController.popBackStack()
                })
            }

            composable(Routes.POMODOROSCREEN + "?taskId={taskId}",
                arguments = listOf(
                    navArgument(name = "taskId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )

            ) {
                PomodoroScreen(onPopBackStack = {
                    navController.popBackStack()

                })

            }
        }


    }






}
