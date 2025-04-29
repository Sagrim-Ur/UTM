package com.example.unchaintaskmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Task(
    val taskName: String,
    //? в конце стринга означает, что это nullable переменная
    val taskDescription: String?,
    val isDone: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)
