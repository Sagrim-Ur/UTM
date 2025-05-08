package com.example.unchaintaskmanager.ui.theme.pomodoro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.unchaintaskmanager.MainActivity
import com.example.unchaintaskmanager.R
import com.example.unchaintaskmanager.data.PomodoroRepository
import com.example.unchaintaskmanager.data.PomodoroSession
import com.example.unchaintaskmanager.data.PomodoroState
import com.example.unchaintaskmanager.data.SessionState
import com.example.unchaintaskmanager.util.IntervalTracker

@AndroidEntryPoint  // Hilt поддерживает Service 
class PomodoroService : LifecycleService() {

    companion object {
        const val ACTION_START     = "com.example.unchaintaskmanager.ACTION_START_POMODORO"
        const val ACTION_PAUSE     = "com.example.unchaintaskmanager.ACTION_PAUSE_POMODORO"
        const val ACTION_RESUME    = "com.example.unchaintaskmanager.ACTION_RESUME_POMODORO"
        const val ACTION_STOP      = "com.example.unchaintaskmanager.ACTION_STOP_POMODORO"
        const val ACTION_ADD_TIME  = "com.example.unchaintaskmanager.ACTION_ADD_TIME"
        const val EXTRA_TASK_ID    = "com.example.unchaintaskmanager.EXTRA_TASK_ID"
        const val EXTRA_OFFSET     = "com.example.unchaintaskmanager.EXTRA_OFFSET_MS"
        const val EXTRA_SESSION_ID = "com.example.unchaintaskmanager.EXTRA_SESSION_ID"
        const val ACTION_RESET = "com.example.unchaintaskmanager.ACTION_RESET_POMODORO"

        private const val CHANNEL_ID      = "pomodoro_channel"
        private const val CHANNEL_NAME    = "Pomodoro Timer"
        private const val NOTIFICATION_ID = 1

        private const val WORK_DURATION  = 25 * 60 * 1000L
        private const val BREAK_DURATION = 5 * 60 * 1000L



    }

    private var isRunning = false
    private var isPaused  = false

    @Inject
    lateinit var pomodoroRepository: PomodoroRepository

    private val intervalTracker = IntervalTracker()
    private var timer: CountDownTimer? = null
    private var isWorkPeriod = true
    private var workDuration = WORK_DURATION
    private var breakDuration = BREAK_DURATION
    private var remainingMs: Long = 0L
    private var pomodoroSession: PomodoroSession? = null
    private var currentTaskId: Int = -1

    override fun onCreate() {
        super.onCreate() // здесь Hilt инъекция уже выполнена
        createNotificationChannel()
        remainingMs = workDuration
        isWorkPeriod = true
    }

    /*
    получается, что одна часть приложения генерит интенты для сервиса,
    а вторая - тот самый сервис, отдаёт эти интенты в OnStartCommand,
    и оно уже выполняет нужные действия.
    То есть приложение поддерживает свой собственный сервис, кидаясь само в себя интентами
     */
    //И вот конкретно эта штука вызывается при любой команде для сервиса, поэтому то, что должно вообще всегда срабатывать - сюда
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // обязательно вызываем super, чтобы LifecycleService корректно обрабатывал события
        super.onStartCommand(intent, flags, startId)
        // Всегда переводим сервис в foreground сразу после старта
             startForeground(
               NOTIFICATION_ID,
         buildNotification(remainingMs, isWorkPeriod)
               )
        when (intent?.action) {
            ACTION_START -> handleStart(intent)
            ACTION_PAUSE -> {pausePomodoro()
                Log.d("ViewModel", "Action Pause вызвалась, ёпт")}
            ACTION_RESUME -> resumePomodoro()    // ← здесь
            ACTION_ADD_TIME -> addTimeToTimer(intent.getLongExtra(EXTRA_OFFSET, 0L))
            ACTION_RESET  -> resetPomodoro()
            ACTION_STOP -> stopPomodoro()
        }
        return START_STICKY
    }

    //Это аналог старта в pomodoroviewmodel
    private fun handleStart(intent: Intent) {
        val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        val offset = intent.getLongExtra(EXTRA_OFFSET, 0L)
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, -1)
        currentTaskId = taskId

        //Готовим таймер
        isWorkPeriod = true

        remainingMs  = WORK_DURATION - offset

        // 4) Сбрасываем и запускаем IntervalTracker
        intervalTracker.reset()

        intervalTracker.start(isWorkPeriod)

        // 5) Переводим сервис в foreground с уведомлением
        startForeground(
            NOTIFICATION_ID,
            buildNotification(remainingMs, isWorkPeriod)
        )

        lifecycleScope.launch {
            // 1) Попытка загрузить уже существующую сессию
            pomodoroSession = if (sessionId != -1) {
                withContext(Dispatchers.IO) {
                    pomodoroRepository.getSessionById(sessionId)
                }
            } else null

            // 2) Если сессии нет — создаём новую в базе
            if (pomodoroSession == null) {
                val newSession = withContext(Dispatchers.IO) {
                    pomodoroRepository.insertSession(
                        PomodoroSession(
                            linkedTaskId = taskId,
                            startTime    = System.currentTimeMillis()
                        )
                    )
                }
                pomodoroSession = newSession
            }

            // 6) Наконец — запускаем собственно обратный отсчёт
            startTimer(offset)
        }
    }


    private fun startTimer(offsetTime: Long = 0L) {
        //Выключаем отсчёт, если он уже идёт
        timer?.cancel()
        //Снимаем с паузы на всякий случай
        isPaused = false
        //Запускаем таймер
        isRunning = true
        // Определяем длительность текущего интервала (работа или перерыв)
        val duration = if (isWorkPeriod) (workDuration - offsetTime) else (breakDuration - offsetTime)
        // Сохраняем оставшееся время в состоянии (для UI и паузы)
        remainingMs = duration
        // сразу обновляем уведомление и рассылаем «запущено»
        updateNotification(remainingMs)
        pomodoroRepository.publishTick(remainingMs, isWorkPeriod, SessionState.RUNNING)
        //Запускаем отслеживание интервалов
        intervalTracker.start(isWorkPeriod)
        //CountDownTimer - абстрактный, приходится переопределять
        timer = object : CountDownTimer(remainingMs, 1000L) {
            override fun onTick(msUntilFinished: Long) {
                remainingMs = msUntilFinished
                updateNotification(remainingMs)
                pomodoroRepository.publishTick(remainingMs, isWorkPeriod, SessionState.RUNNING)
                lifecycleScope.launch(Dispatchers.IO) {
                    pomodoroRepository.saveState(
                        PomodoroState(
                            remainingMs  = remainingMs,
                            isWorkPeriod = isWorkPeriod,
                            sessionState = SessionState.RUNNING
                        )
                    )
                }
            }
            override fun onFinish() {
                // Заканчиваем текущий интервал
                intervalTracker.stop()
                //Меняем работу на отдых и наоборот
                isWorkPeriod = !isWorkPeriod
                //Сбрасываем отсчёт для периода, который будем считать дальше
                remainingMs = if (isWorkPeriod) workDuration else breakDuration
                //Обнуляем сколько времени прошло
                // уведомляем сразу о смене периода
                pomodoroRepository.publishTick(
                    remainingMs,
                    isWorkPeriod,
                    SessionState.RUNNING
                )

                if (!isWorkPeriod) startTimer() else {
                    onPomodoroComplete()
                }

            }
        }.start()
    }

    private fun pausePomodoro() {
        //Этот блок полность соответствует старой логике
        timer?.cancel() //отменяем
        isRunning = false //считаем, что таймер не работает
        isPaused = true //теперь считаем, что на паузе
        intervalTracker.pause()
        //Это новая логика
        // обновляем уведомление
        updateNotification(remainingMs)
        //Говорим, что  рассылаем «на паузе»
        pomodoroRepository.publishTick(remainingMs, isWorkPeriod, SessionState.PAUSED)
        lifecycleScope.launch(Dispatchers.IO) {
            pomodoroRepository.saveState(
                PomodoroState(
                    remainingMs  = remainingMs,
                    isWorkPeriod = isWorkPeriod,
                    sessionState = SessionState.PAUSED
                )
            )
            Log.d("PomodoroServise", "Изменение состояния на паузу отработало")
        }
        onIntervalComplete()
    }

    private fun resumePomodoro() {
        if (!isPaused) return
        isPaused = false
        isRunning = true

        // сразу уведомляем UI, что мы возвращаемся в работающий режим
        pomodoroRepository.publishTick(remainingMs, isWorkPeriod, SessionState.RUNNING)
        lifecycleScope.launch(Dispatchers.IO) {
            pomodoroRepository.saveState(
                PomodoroState(remainingMs, isWorkPeriod, SessionState.RUNNING)
            )
        }

        // теперь пересоздаём таймер с нужным оффсетом
        val unpauseOffset = (if (isWorkPeriod) WORK_DURATION else BREAK_DURATION) - remainingMs
        startTimer(unpauseOffset)
    }

    private fun resetPomodoro() {
        timer?.cancel()
        isRunning = false
        isPaused  = false
        intervalTracker.reset()
        // сбрасываем до начала текущего периода
        remainingMs = if (isWorkPeriod) workDuration else breakDuration
        // обновляем UI через уведомление и репозиторий
        updateNotification(remainingMs)
        pomodoroRepository.publishTick(remainingMs, isWorkPeriod, SessionState.RUNNING)
        lifecycleScope.launch(Dispatchers.IO) {
            pomodoroRepository.saveState(
                PomodoroState(remainingMs, isWorkPeriod, SessionState.RUNNING)
            )
        }
        startTimer()
    }


    private fun addTimeToTimer(addMs: Long) {
        timer?.cancel()
        remainingMs += addMs
        updateNotification(remainingMs)
        // если был паузой — останемся в PAUSED, иначе — в RUNNING
        val state = if (isPaused) SessionState.PAUSED else SessionState.RUNNING
        pomodoroRepository.publishTick(remainingMs, isWorkPeriod, state)
        lifecycleScope.launch(Dispatchers.IO) {
            pomodoroRepository.saveState(
                PomodoroState(
                    remainingMs  = remainingMs,
                    isWorkPeriod = isWorkPeriod,
                    sessionState = state
                )
            )
        }
        if (!isPaused) {
            // пересоздаём таймер «как будто restart»
            val offset = (if (isWorkPeriod) WORK_DURATION else BREAK_DURATION) - remainingMs
            startTimer(offset)
        }
    }

    private fun onPomodoroComplete(startNextPomodoro: Boolean = false) {
        // Сохраняем итоги до сброса
        val totalWork = intervalTracker.getTotalWorkTime()
        val totalRest = intervalTracker.getTotalRestTime()
        lifecycleScope.launch {
            try {
                // 1) Обновляем текущую сессию
                pomodoroSession?.let { session ->
                    withContext(Dispatchers.IO) {
                        pomodoroRepository.updateSession(
                            session.copy(
                                endTime      = System.currentTimeMillis(),
                                workDuration = totalWork,
                                restDuration = totalRest
                            )
                        )
                    }
                }

                if (startNextPomodoro) {
                    // Создаём новую сессию и сохраняем
                    // 2) Создаём новую сессию
                    val newSession = withContext(Dispatchers.IO) {
                        pomodoroRepository.insertSession(
                            PomodoroSession(
                                linkedTaskId = currentTaskId,
                                startTime = System.currentTimeMillis()
                            )
                        )
                    }
                    pomodoroSession = newSession

                    // 3) Сбрасываем трекер и стартуем новый таймер на работу
                    intervalTracker.reset()
                    isWorkPeriod = true
                    startTimer()
                }
                else {
                    // Финальное завершение — просто сбрасываем трекер и обнуляем ссылку
                    intervalTracker.reset()
                    isRunning = false
                    isPaused = false
                    pomodoroSession = null
                    //Сбрасываем отсчёт для периода, который будем считать дальше
                    isWorkPeriod = true
                    remainingMs = workDuration
                    pomodoroRepository.publishTick(
                        remainingMs    = remainingMs,
                        isWorkPeriod   = isWorkPeriod,
                        sessionState   = SessionState.STOPPED
                    )


                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка при завершении pomodoro", e)
            }
        }
    }


    private fun stopPomodoro() {
        timer?.cancel()
        intervalTracker.stop()
        isRunning = false //считаем, что таймер не работает
        isPaused = false //и не на паузе
        onPomodoroComplete(startNextPomodoro = false)
        pomodoroRepository.publishTick(
            remainingMs    = remainingMs,
            isWorkPeriod   = isWorkPeriod,
            sessionState   = SessionState.STOPPED
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onIntervalComplete() {
        //Сохраняем в базу в конце каждого интервала
        val totalWorkTimeSoFar = intervalTracker.getTotalWorkTime()
        val totalRestTimeSoFar = intervalTracker.getTotalRestTime()
        lifecycleScope.launch {
            try {
                // В IO-пуле выполняем updateSession, чтобы не блокировать Main-поток
                withContext(Dispatchers.IO) {

                    pomodoroSession?.let {
                        pomodoroRepository.updateSession(
                            it.copy(
                                endTime = System.currentTimeMillis(),
                                workDuration = totalWorkTimeSoFar,
                                restDuration = totalRestTimeSoFar
                            )
                        )
                    }
                }
                Log.d(
                    "ViewModel",
                    "Сохранено в базу с $totalWorkTimeSoFar рабочего времени, $totalRestTimeSoFar времени отдыха"
                )
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка onIntervalComplete", e)
                //_uiEvent.send(UiEvent.ShowError("Не удалось сохранить промежуточный результат"))
            }

        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Pomodoro Timer Notifications"
                setShowBadge(false)
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                    .createNotificationChannel(this)
            }
        }
    }

    private fun buildNotification(remainingMs: Long, isWork: Boolean): Notification {
        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val minutes = (remainingMs / 1000) / 60
        val seconds = (remainingMs / 1000) % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(if (isWork) "Работа" else "Перерыв")
            .setContentText("Осталось: $timeText")
            .setSmallIcon(R.drawable.steak)  // замените steak на свой ресурс
            .setContentIntent(pi)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .notify(NOTIFICATION_ID, buildNotification(remainingMs, isWorkPeriod))
    }
}
