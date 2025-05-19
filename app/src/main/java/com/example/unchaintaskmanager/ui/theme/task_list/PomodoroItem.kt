package com.example.unchaintaskmanager.ui.theme.pomodoro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.unchaintaskmanager.data.PomodoroSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSessionItem(
    pomodoro: PomodoroSession,
    modifier: Modifier = Modifier
   ) {
    // Форматер времени
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }
    val startStr = timeFormatter.format(Date(pomodoro.startTime))
    val endStr = pomodoro.endTime?.takeIf { it > 0L }
        ?.let { timeFormatter.format(Date(it)) }
        ?: "--:--:--"

    // Форматируем длительности
    fun msToMinSec(ms: Long): String {
        val m = ms / 1000 / 60
        val s = ms / 1000 % 60
        return "%d:%02d".format(m, s)
    }
    val workStr = msToMinSec(pomodoro.workDuration)
    val restStr = msToMinSec(pomodoro.restDuration)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить сессию"
                )
            }

            Spacer(Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Первая строка: начало — конец
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = startStr,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " – ",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Light
                    )
                    Text(
                        text = endStr,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Вторая строка: Work и Break
                Row {
                    Text(
                        text = "Work:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = workStr,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Break:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = restStr,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
/*
@Composable
fun PomodoroSessionItem(
    pomodoro: PomodoroSession,
    modifier: Modifier = Modifier
)

{
    // 1) Formatter для времени старта/конца
    val timeFormatter = remember {
        SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }

    // 2) Превращаем миллисекунды в читабельную строку
    val startStr = timeFormatter.format(Date(pomodoro.startTime))
    val endStr = if (pomodoro.endTime != null && pomodoro.endTime > 0L)
        timeFormatter.format(Date(pomodoro.endTime))
    else
        "--:--:--"

    // 3) Вычисляем длительности (ms → mm:ss)
    val workMs = pomodoro.workDuration
    val workMin = (workMs / 1000 / 60)
    val workSec = (workMs / 1000 % 60)
    val workStr = "%d:%02d".format(workMin, workSec)

    val restMs = pomodoro.restDuration
    val restMin = restMs / 1000 / 60
    val restSec = (restMs / 1000 % 60)
    val restStr = "%d:%02d".format(restMin, restSec)

    //Ряд, в котором располагается воообще всё
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = startStr,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = endStr,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Work: $workStr",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Break: $restStr",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

 */