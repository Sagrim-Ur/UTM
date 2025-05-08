package com.example.unchaintaskmanager.dependencyinjection

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.unchaintaskmanager.data.MIGRATION_1_2
import com.example.unchaintaskmanager.data.PomodoroRepository
import com.example.unchaintaskmanager.data.PomodoroRepositoryImplementation
import com.example.unchaintaskmanager.data.TaskDatabase
import com.example.unchaintaskmanager.data.TaskRepository
import com.example.unchaintaskmanager.data.TaskRepositoryImplementation
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object AppModule {

    //Этот код создаёт и предоставляет экземпляр базы данных TaskDatabase, который будет доступен через Hilt.
    //Благодаря @Singleton база данных будет существовать в одном экземпляре для всего приложения, что повышает производительность и предотвращает утечки памяти.
    @Provides
    @Singleton
    fun provideTaskDatabase(app: Application): TaskDatabase {
        return Room.databaseBuilder(
            app,
            TaskDatabase::class.java,
            "tasks_db"
        ).addMigrations(MIGRATION_1_2) // Добавляем миграцию
            .build()
    }


    //Этот код предоставляет объект TasksRepositoryImplementation, который подключается к базе данных через DAO. Использование Hilt и аннотации @Singleton позволяет гарантировать, что репозиторий создаётся один раз и автоматически доступен там, где это нужно.
    @Provides
    @Singleton
    fun provideTaskRepository(db: TaskDatabase): TaskRepository
    {
        return TaskRepositoryImplementation(db.dao)
    }

    //Этот код предоставляет объект PomodoroRepositoryImplementation, который подключается к базе данных через DAO. Использование Hilt и аннотации @Singleton позволяет гарантировать, что репозиторий создаётся один раз и автоматически доступен там, где это нужно.
    @Provides
    @Singleton
    fun providePomodoroRepository(db: TaskDatabase, prefs: SharedPreferences): PomodoroRepository
    {
        return PomodoroRepositoryImplementation(
            dao   = db.pomodoroDao,
            prefs = prefs)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(app: Application): SharedPreferences {
        // "pomodoro_prefs" — имя файла настроек,
        // MODE_PRIVATE — доступ только вашему приложению
        return app.getSharedPreferences("pomodoro_prefs", Context.MODE_PRIVATE)
    }


}