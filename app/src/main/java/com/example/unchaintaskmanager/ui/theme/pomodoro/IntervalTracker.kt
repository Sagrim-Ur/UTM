package com.example.unchaintaskmanager.ui.theme.pomodoro

import android.os.SystemClock
import android.util.Log

/**
 * Отслеживает рабочие и перерывные интервалы с возможностями запуска, паузы, остановки и сброса.
 * Гарантирует потокобезопасность и использует монотонный таймер для точного измерения времени.
 */


class IntervalTracker {
    // Внутренний класс для хранения начала и конца интервала
    private data class TimeInterval(val start: Long, var end: Long? = null)

    // Списки завершённых рабочих и перерывных интервалов
    private val workIntervals = mutableListOf<TimeInterval>()
    private val restIntervals = mutableListOf<TimeInterval>()

    // Текущий активный интервал
    private var currentInterval: TimeInterval? = null
    // Флаг: рабочий ли это интервал
    private var isWorkPeriod: Boolean = true
    // Флаг: трекер остановлен (нет активного интервала)
    private var isStopped: Boolean = true

    /**
     * Запускает новый интервал.
     * Если до этого уже был активный, сначала ставит его на паузу.
     * @param isWork true — рабочий интервал, false — интервал отдыха
     */
    @Synchronized
    fun start(isWork: Boolean) {
        if (!isStopped) {
            // Закрываем предыдущий активный интервал
            pause()
        }
        isWorkPeriod = isWork
        // Запоминаем время начала интервала
        currentInterval = TimeInterval(SystemClock.elapsedRealtime())
        isStopped = false
        Log.d("IntervalTracker", "Запущен ${if (isWorkPeriod) "рабочий" else "перерывный"} интервал в ${currentInterval!!.start} мс")

    }

    /**
     * Ставит текущий интервал на паузу и сохраняет его в список.
     * Если нет активного интервала, выводит предупреждение.
     */
    @Synchronized
    fun pause() {
        val interval = currentInterval ?: run {
            Log.w("IntervalTracker", "pause() вызван, но активного интервала нет")
            return
        }
        // Фиксируем время окончания
        interval.end = SystemClock.elapsedRealtime()
        // Добавляем в соответствующий список
        if (isWorkPeriod) workIntervals.add(interval) else restIntervals.add(interval)
        isStopped = true
        currentInterval = null
        logSummary()
    }

    /**
     * Останавливает трекер, завершая любой активный интервал.
     */
    @Synchronized
    fun stop() {
        if (currentInterval != null) {
            Log.d("IntervalTracker", "stop() вызван, завершаем активный интервал")
            pause()
        }
        isStopped = true
        Log.d("IntervalTracker", "Трекер остановлен" +
                "Работа: count=${workIntervals.size}, total=${getTotalWorkTime()} мс; " +
                "Отдых: count=${restIntervals.size}, total=${getTotalRestTime()} мс")
    }


    /**
     * Возвращает общее время работы, включая незавершённый интервал.
     */
    @Synchronized
    fun getTotalWorkTime(): Long {
        // Сумма завершённых интервалов
        val closed = workIntervals.sumOf { it.end!! - it.start }
        // Время текущего активного интервала (если он рабочий)
        val ongoing = if (currentInterval != null && isWorkPeriod) {
            SystemClock.elapsedRealtime() - currentInterval!!.start
        } else 0L
        return closed + ongoing
    }

    /**
     * Возвращает общее время отдыха, включая незавершённый интервал.
     */
    @Synchronized
    fun getTotalRestTime(): Long {
        val closed = restIntervals.sumOf { it.end!! - it.start }
        val ongoing = if (currentInterval != null && !isWorkPeriod) {
            SystemClock.elapsedRealtime() - currentInterval!!.start
        } else 0L
        return closed + ongoing
    }

    /**
     * Сбрасывает трекер в начальное состояние, очищая все интервалы.
     */
    @Synchronized
    fun reset() {
        workIntervals.clear()
        restIntervals.clear()
        currentInterval = null
        isWorkPeriod = true
        isStopped = true
        Log.d("IntervalTracker", "Трекер сброшен в начальное состояние")
    }

    /**
     * Выводит в лог краткую статистику: количество интервалов и общее время.
     */
    private fun logSummary() {
        Log.d("IntervalTracker",
            "Работа: count=${workIntervals.size}, total=${getTotalWorkTime()} мс; " +
                    "Отдых: count=${restIntervals.size}, total=${getTotalRestTime()} мс"
        )
    }


}