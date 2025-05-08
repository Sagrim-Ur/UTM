package com.example.unchaintaskmanager.ui.theme.pomodoro

import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unchaintaskmanager.data.PomodoroRepository
import com.example.unchaintaskmanager.data.PomodoroSession
import com.example.unchaintaskmanager.data.TaskRepository
import com.example.unchaintaskmanager.util.IntervalTracker
import com.example.unchaintaskmanager.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.example.unchaintaskmanager.data.SessionState

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    //Сколько времени занимает помодоро. Потом поменяем на значение из базы
    private val workDuration = 25 * 60 * 1000L

    //Сколько времени занимает переыв. Потом поменяем на значение из базы
    private val breakDuration = 5 * 60 * 1000L

    //Сколько времени осталось на Pomodoro
    private val _timeLeft = mutableStateOf(workDuration)
    val timeLeft: State<Long> get() = _timeLeft

    //Таймер запущен?
    var isRunning by mutableStateOf(false)
        private set

    //У нас работа или перерыв? Если true - работа, если false - перерыв
    var isWorkPeriod by mutableStateOf(true)
        private set



    private val intervalTracker = IntervalTracker()

    //Таймер обратного отсчёта
    private var timer: CountDownTimer? = null

    //тут мы сохраняем текущую сессию, чтобы было удобно её прерывать, etc.
    var pomodoroSession by mutableStateOf<PomodoroSession?>(null)
    private set

    // Флаг: таймер был поставлен на паузу
    var isPaused by mutableStateOf(false)

    // Время, когда была нажата пауза (нужно для восстановления)
    private var timerStartTimestamp: Long = 0L


    private val _uiEvent = Channel<UiEvent>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _taskTitle = MutableStateFlow("Загрузка задачи...")
    val taskTitle: StateFlow<String> = _taskTitle

    val taskId = savedStateHandle.get<Int>("taskId")!!
    init {

        // 1) Подписываемся на поток тиков от сервиса
        viewModelScope.launch {
            pomodoroRepository.timerFlow.collect { tick ->
                _uiState.update {
                    it.copy(
                        remainingMs  = tick.remainingMs,
                        isWorkPeriod = tick.isWorkPeriod,
                        sessionState = tick.sessionState
                    )
                }
            }
        }

        // 2) Однократно загружаем последнее сохранённое состояние
        viewModelScope.launch {
            pomodoroRepository.loadState()?.let { state ->
                _uiState.update {
                    it.copy(
                        remainingMs  = state.remainingMs,
                        isWorkPeriod = state.isWorkPeriod,
                        sessionState = state.sessionState
                    )
                }
            }
        }


    //Зачем вот это? Потому что при переходе на экран Pomodoro мы передаём через SavedStateHandle id задачи, на которой была нажата кнопка запуска pomodoro

            Log.d("ViewModel", "Полученный taskId: $taskId")  // 🔍 Проверка
            defineTaskName()
        }

    private fun defineTaskName() {
        viewModelScope.launch {
            if (taskId == -1) {
                _taskTitle.value = "Какая-то по-настоящему важная задача"
            } else {
                val task = repository.getTaskById(taskId)
                _taskTitle.value = task?.taskName ?: "Задача не найдена"
            }
        }
    }




    fun startTimer(offsetTime: Long = 0L) {
        //Выключаем отсчёт, если он уже идёт
        timer?.cancel()
        //Снимаем с паузы на всякий случай
        isPaused = false
        //Запускаем таймер
        isRunning = true
        // Запоминаем время старта таймера
        timerStartTimestamp = System.currentTimeMillis()
        // Определяем длительность текущего интервала (работа или перерыв)
        val duration = if (isWorkPeriod) (workDuration - offsetTime) else (breakDuration - offsetTime)
        // Сохраняем оставшееся время в состоянии (для UI и паузы)
        _timeLeft.value = duration
        //Запускаем отслеживание интервалов
        intervalTracker.start(isWorkPeriod)

        //CountDownTimer - абстрактный, приходится переопределять
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
            }
            override fun onFinish() {
                // Заканчиваем текущий интервал
                intervalTracker.stop()

                //Сокраняем последний текущий интервао
                //intervalTracker.pause()
                //Сохраняем в базу завершающие действия
                //onEvent(PomodoroScreenEvent.onPomodoroTimerStop)
                //сбрасываем интервалы
                //intervalTracker.reset()
                //Меняем работу на отдых и наоборот
                isWorkPeriod = !isWorkPeriod
                //Сбрасываем отсчёт для периода, который будем считать дальше
                _timeLeft.value = if (isWorkPeriod) workDuration else breakDuration
                //Обнуляем сколько времени прошло
                if (!isWorkPeriod) startTimer() else {
                    onPomodoroComplete()
                }

            }
        }.start()
    }

    fun pauseTimer() {
        timer?.cancel() //отменяем
        isRunning = false //считаем, что таймер не работает
        isPaused = true //теперь считаем, что на паузе
        val pauseTimeElapsed = if (isWorkPeriod) (workDuration - (_timeLeft.value)) else (breakDuration - (_timeLeft.value)) //фиксируем сколько прошло времени на таймере в момент паузы
        Log.d("ViewModel", "Помодоро на паузе, прошло $pauseTimeElapsed")
        // Фиксируем паузу в трекере
        intervalTracker.pause()
    }

    fun resetTimer() {
        timer?.cancel()
        isRunning = false
        isPaused = false
        intervalTracker.reset()
        _timeLeft.value = if (isWorkPeriod) workDuration else breakDuration
        Log.d("ViewModel", "таймер сброшен")

    }

    fun stopTimer() {
        timer?.cancel() //отменяем
        isRunning = false //считаем, что таймер не работает
        isPaused = false //и не на паузе
        val stopTimeElapsed = if (isWorkPeriod) (workDuration - (_timeLeft.value)) else (breakDuration - (_timeLeft.value)) //фиксируем сколько прошло времени на таймере в момент паузы
        Log.d("ViewModel", "Помодоро остановлен, прошло $stopTimeElapsed")
        intervalTracker.stop()
    }

    fun resumeTimer() {
        if (isPaused) {
            isPaused = false
            isRunning = true
            val unpauseOffset = if (isWorkPeriod) (workDuration - (_timeLeft.value)) else (breakDuration - (_timeLeft.value))
            Log.d("ViewModel", "Помодоро перезапущен с оффсетом $unpauseOffset")
            startTimer(unpauseOffset) //перезапускаем с учётом паузы
        }

    }

    /**
     * Прибавляет к текущему таймеру addMs миллисекунд **без ограничения сверху**.
     * Если таймер был запущен — перезапускаем его с новым offset,
     * если на паузе — просто обновляем _timeLeft и ждём возобновления.
     */
    private fun addTimeToTimer(addMs: Long) {
        // Отменяем старый CountDownTimer
        timer?.cancel()

        // Вычисляем новую оставшуюся длительность
        val newTimeLeft = _timeLeft.value + addMs
        _timeLeft.value = newTimeLeft

        // Если таймер в работе — пересчитываем offset и перезапускаем
        if (isRunning) {
            // Берём «базовую» длительность текущего периода
            val base = if (isWorkPeriod) workDuration else breakDuration
            // offset = сколько уже прошло: базовая минус текущее left —
            // если newTimeLeft > base, offset получится отрицательным, и startTimer
            // даст duration = base - offset = newTimeLeft
            val offset = base - newTimeLeft
            startTimer(offset)
        }
        // Если на паузе — оставляем isPaused = true,
        // и при resumeTimer() таймер продолжит с этим новым _timeLeft
    }

    private fun onIntervalComplete() {
        //Сохраняем в базу в конце каждого интервала
        val totalWorkTimeSoFar = intervalTracker.getTotalWorkTime()
        val totalRestTimeSoFar = intervalTracker.getTotalRestTime()
        viewModelScope.launch {
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

    private fun onPomodoroComplete(startNextPomodoro: Boolean = false) {
        // Сохраняем итоги до сброса
        val totalWork = intervalTracker.getTotalWorkTime()
        val totalRest = intervalTracker.getTotalRestTime()
        viewModelScope.launch {
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
                                linkedTaskId = taskId,
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
                    _timeLeft.value = workDuration
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Ошибка при завершении pomodoro", e)
            }
        }
    }


    fun onEvent(event: PomodoroScreenEvent) {
        when (event) {
            //при переходе на страницу pomodoro с другой страницы по нажатию кнопки, или запуске таймера при нажатии кнопки запустить
            PomodoroScreenEvent.OnStartPomodoro -> {
                isWorkPeriod = true
                viewModelScope.launch {

                    try {
                    // 1) Сбрасываем трекер до старта
                    intervalTracker.reset()

                    // 2) Вставляем новую сессию и ждём её готовности
                    val startTime = System.currentTimeMillis()
                    val newSession = withContext(Dispatchers.IO) {
                        pomodoroRepository.insertSession(
                            PomodoroSession(
                                linkedTaskId = taskId,
                                startTime = startTime
                            )
                        )
                    }
                    pomodoroSession = newSession

                    // 3) Только после вставки — запускаем таймер
                    startTimer()
                } catch (e: Exception) {
                    Log.e("ViewModel", "Ошибка создания первой Pomodoro-сессии", e)
                }
                }


            }

            PomodoroScreenEvent.OnPausePomodoro -> {
            //При нажатии кнопки Пауза
                pauseTimer()
                onIntervalComplete()

            }

            PomodoroScreenEvent.OnRestartPomodoro -> {
                //При нажатии кнопки перезапуск
                resetTimer()


            }

            PomodoroScreenEvent.OnResumePomodoro -> {
                //при нажатии кнопки Возобновить (после пазуы)
                resumeTimer()
            }

            PomodoroScreenEvent.onFinishPomodoro -> {
                //При нажатии кнопки Завершить Pomodoro
                stopTimer()
                onPomodoroComplete(startNextPomodoro = false)
            }

            PomodoroScreenEvent.plusFifteenMinutesToPomodoro -> {
                addTimeToTimer(15 * 60_000L)
            }
            PomodoroScreenEvent.plusFiveMinutesToPomodoro -> { addTimeToTimer(5 * 60_000L) }
            PomodoroScreenEvent.plusThirtyMinutesToPomodoro -> { addTimeToTimer(30 * 60_000L) }
        }
    }


    fun startPomodoro(context: Context, taskId: Int, offsetMs: Long = 0L) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_START
            it.putExtra(PomodoroService.EXTRA_TASK_ID, taskId)
            it.putExtra(PomodoroService.EXTRA_OFFSET, offsetMs)
            ContextCompat.startForegroundService(context, it)
        }
    }

    fun pausePomodoro(context: Context) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_PAUSE
            ContextCompat.startForegroundService(context, it)
        }
        Log.d("ViewModel", "Функция паузы во ViewModel отработала")
    }

    fun resumePomodoro(context: Context) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_RESUME
            ContextCompat.startForegroundService(context, it)
        }
        Log.d("ViewModel", "Функция возобновления во ViewModel отработала")}



    fun addTime(context: Context, ms: Long) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_ADD_TIME
            it.putExtra(PomodoroService.EXTRA_OFFSET, ms)
            ContextCompat.startForegroundService(context, it)
        }
    }

    fun stopPomodoro(context: Context) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_STOP
            ContextCompat.startForegroundService(context, it)      // ← обычный startService
        }
    }

    fun resetPomodoro(context: Context) {
        Intent(context, PomodoroService::class.java).also {
            it.action = PomodoroService.ACTION_RESET
            ContextCompat.startForegroundService(context, it)      // ← обычный startService
        }
    }


}

