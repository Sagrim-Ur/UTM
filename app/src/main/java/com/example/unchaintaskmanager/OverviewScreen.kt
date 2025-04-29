package com.example.unchaintaskmanager

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.unchaintaskmanager.ui.theme.task_list.TaskItem
import com.example.unchaintaskmanager.ui.theme.task_list.TaskListViewModel
import com.example.unchaintaskmanager.ui.theme.task_list.TaskScreenEvent

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Это базовое представление
@Composable
fun OverviewScreen(onNavigateToSecondScreen: () -> Unit, viewModel: TaskListViewModel = hiltViewModel()) {
    val context = LocalContext.current

    //Здесь мы наполняем список задачами из TaskListViewModel
    val tasks = viewModel.tasks.collectAsState(initial = emptyList())



    Column(
        modifier = Modifier.wrapContentSize(),
        Arrangement.SpaceEvenly
    ) {

        //Это поверхность, на которой расположена текущая цель
        Surface(
            modifier = Modifier.padding(8.dp),
            shadowElevation = 4.dp,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Ближайшая цель")
                }
                GoalCard("Цель 1") {
                    Toast.makeText(context, "Нажата цель 1", Toast.LENGTH_SHORT).show()
                }
            }
        }




        //Это поверхность, на которой расположены задачи
        Surface(
            modifier = Modifier.padding(8.dp),
            shadowElevation = 4.dp,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth().padding(6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("Задачи в работе")
                }
                LazyColumn(
                    modifier = Modifier.wrapContentSize()
                ) {
                    items(tasks.value.takeLast(3)) { task ->
                        TaskItem(task = task, onEvent = viewModel::onEvent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable{
                                    viewModel.onEvent((TaskScreenEvent.OnTaskClick(task)))
                                }
                        )


                    }
                }            }
        }

        //Это ряд, в котором следующая встреча и кнопки
        Row (
            verticalAlignment = Alignment.Top
        )
        {

            //Это поверхность, на которой расположена следующая встреча
            Surface(
                modifier = Modifier.padding(8.dp).fillMaxWidth(0.5f),
                shadowElevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.wrapContentSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(6.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text("Ближайшая встреча")
                    }

                    MeetingCard(LocalDateTime.now(), "Встреча 1") {
                        Toast.makeText(
                            context,
                            "Нажата встреча 1",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }


            //Это контейнер для кнопок
            Box(
                modifier = Modifier.wrapContentSize(),
                contentAlignment = Alignment.Center
            ) {
                // Сетка из 2 строк по 2 кнопки
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    //Вержний ряд кнопок
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Первая кнопка
                        FloatingActionButton(
                            onClick = { Toast.makeText(
                                context,
                                "Нажата кнопка 1",
                                Toast.LENGTH_SHORT
                            ).show() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
                        }
                        // Вторая кнопка
                        FloatingActionButton(
                            onClick = { Toast.makeText(
                                context,
                                "Нажата кнопка 2",
                                Toast.LENGTH_SHORT
                            ).show() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Star, contentDescription = "Remove")
                        }
                    }
                    //Нижний ряд кнопок
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Третья кнопка
                        FloatingActionButton(
                            onClick = { Toast.makeText(
                                context,
                                "Нажата кнопка 3",
                                Toast.LENGTH_SHORT
                            ).show() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search")
                        }
                        // Четвертая кнопка
                        FloatingActionButton(
                            onClick = { Toast.makeText(
                                context,
                                "Нажата кнопка 4",
                                Toast.LENGTH_SHORT
                            ).show() },
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    }
                }
            }
        }



    }


}


//Это карточка для отображения задач
@Composable
fun TaskCard(
    title: String,
    description: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Индикатор выполнения
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onClick() } // Можно обновить состояние задачи
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Основная информация о задаче
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

//Это карточка для отображения встречи
@Composable
fun MeetingCard(
    dateTime: LocalDateTime,
    meetingDescription: String,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = meetingDescription,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )


        }

    }
}


//Это карточка для отображения цели
@Composable
fun GoalCard(
    goalDescription: String,
    onClick: () -> Unit
) {

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goalDescription,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

//Это текстовое поле для отладки
@Composable
fun SimpleTextField() {
    var text by remember { mutableStateOf("") } // Состояние для хранения текста

    Column {
        TextField(
            value = text,
            onValueChange = { newText -> text = newText }, // Обработчик изменения текста
            label = { Text("Введите текст") } // Подпись для поля
        )
    }
}

//Это круглая кнопка
@Composable
fun FloatingActionButtonExample() {
    Box(
        modifier = Modifier.fillMaxSize(), // Заполнение доступного пространства
        contentAlignment = Alignment.BottomEnd // Выравнивание по нижнему правому углу
    ) {
        FloatingActionButton(
            onClick = { /* Действие при нажатии */ },
            containerColor = Color.Blue, // Цвет фона кнопки
            contentColor = Color.White, // Цвет содержимого (иконки)
            modifier = Modifier.padding(16.dp) // Отступы от краёв экрана
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Добавить") // Иконка на кнопке
        }
    }
}