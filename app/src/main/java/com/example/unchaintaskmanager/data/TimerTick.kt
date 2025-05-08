package com.example.unchaintaskmanager.data


/**
 * DTO для передачи из PomodoroService в UI:
 * @param remainingMs — сколько миллисекунд осталось
 * @param isWorkPeriod — true, если сейчас рабочий период, false — перерыв
 * DTO (Data Transfer Object) — это простой класс-контейнер без бизнес-логики,
 * который служит для передачи данных между слоями вашего приложения (например, из сервиса в ViewModel через репозиторий).
 * DTO обычно содержит только свойства (поля) и, возможно, методы-утилиты (например, toString или copy),
 * но никакой логики, связанной с хранением или отображением.
 */
data class TimerTick(
    val remainingMs: Long,
    val isWorkPeriod: Boolean,
    val sessionState: SessionState
)