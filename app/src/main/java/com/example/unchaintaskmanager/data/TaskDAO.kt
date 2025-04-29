package com.example.unchaintaskmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

//Это интерфейс. Мы тут определяем, как мы получаем доступ к базе
@Dao
interface TaskDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM Task WHERE id = :id")
    suspend fun getTaskById(id: kotlin.Int): Task?

    @Query("SELECT * FROM Task")
    fun getTasks(): Flow<List<Task>>

}