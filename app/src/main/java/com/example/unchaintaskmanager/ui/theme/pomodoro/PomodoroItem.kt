package com.example.unchaintaskmanager.ui.theme.pomodoro

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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