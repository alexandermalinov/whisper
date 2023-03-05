package com.example.whisper.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeFormatter {

    /* --------------------------------------------------------------------------------------------
     * Private
    ---------------------------------------------------------------------------------------------*/
    private val hourMinuteFormat = SimpleDateFormat("kk:mm aa", Locale.US)
    private val weekDayFormat = SimpleDateFormat("EEEE", Locale.US)
    private val dayOfYearFormat = SimpleDateFormat("EEEE MMM d yyyy", Locale.US)
    private val dateDayMinutesFormat = SimpleDateFormat("E MMM dd", Locale.US)

    /* --------------------------------------------------------------------------------------------
     * Exposed
    ---------------------------------------------------------------------------------------------*/
    fun formatMessageDateTime(timestamp: Long): String = hourMinuteFormat.format(timestamp)

    fun formatWeekDay(timestamp: Long): String = weekDayFormat.format(timestamp)

    fun formatDayOfYear(timestamp: Long): String = dayOfYearFormat.format(timestamp)

    fun formatDayMinutes(timestamp: Long): String = dateDayMinutesFormat.format(timestamp)
}