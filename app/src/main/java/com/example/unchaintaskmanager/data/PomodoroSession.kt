package com.example.unchaintaskmanager.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Task::class,
        parentColumns = ["id"],
        childColumns = ["linkedTaskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val pomodoroId: Int? = null,
    val linkedTaskId: Int? = null,  // Может быть NULL, если сессия пока не привязана
    val startTime: Long,
    val endTime: Long? = null,
    val workDuration: Long = 0,
    val restDuration: Long = 0,
)