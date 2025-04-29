package com.example.unchaintaskmanager.data

import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//Это класс базы данных

@Database(
    entities = [Task::class, PomodoroSession::class],
    version = 2
)

abstract class TaskDatabase: RoomDatabase() {

    abstract val dao: TaskDAO
    abstract val pomodoroDao: PomodoroDAO
}


val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Создание таблицы с правильными параметрами
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS PomodoroSession (
                startTime INTEGER NOT NULL,
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                endTime INTEGER,
                taskId INTEGER,
                FOREIGN KEY(taskId) REFERENCES Task(id) ON DELETE CASCADE            )
            """
        )
    }
}