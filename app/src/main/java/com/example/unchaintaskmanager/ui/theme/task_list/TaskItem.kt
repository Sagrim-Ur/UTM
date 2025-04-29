package com.example.unchaintaskmanager.ui.theme.task_list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unchaintaskmanager.data.Task

@Composable
fun TaskItem(
    task: Task,
    onEvent: (TaskScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    //Основное тело карточки
    Card(

        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        //Ряд, в котором располагается воообще всё
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Колонка, в которой название, содержание и кнопка "удалить"
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                //Ряд, в котором название и кнопка удалить
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.taskName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                        Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {
                        onEvent(TaskScreenEvent.DeleteTask(task))
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }

                }
                task.taskDescription?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = it,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            )

                }

            }

            Button(
                onClick =  {
                    onEvent(TaskScreenEvent.OnPomodoroClickEvent(task))
                },
                shape = RoundedCornerShape(16.dp), // Скругленные углы
                border = BorderStroke(2.dp, Color.Black), // Обводка
                modifier = Modifier
                          .padding(top = 8.dp)

            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Кнопка запуска таймера Pomodoro")
            }

            Checkbox(checked = task.isDone,
                onCheckedChange = { isChecked ->
                    onEvent(TaskScreenEvent.OnDoneChange(task, isChecked))
                }
            )
        }
    }
}