package com.example.unchaintaskmanager.ui.theme.pomodoro

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unchaintaskmanager.data.PomodoroRepository
import com.example.unchaintaskmanager.data.PomodoroSession
import com.example.unchaintaskmanager.data.TaskRepository
import com.example.unchaintaskmanager.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val pomodoroRepository: PomodoroRepository,
    private val repository: TaskRepository,
    savedStateHandle: SavedStateHandle
): ViewModel() {

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

    private fun onPomodoroComplete(startNextPomodoro: Boolean = true) {
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
        }
    }

}