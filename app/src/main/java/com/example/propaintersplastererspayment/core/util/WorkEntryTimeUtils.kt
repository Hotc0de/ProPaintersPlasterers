package com.example.propaintersplastererspayment.core.util

import java.util.Locale

object WorkEntryTimeUtils {
    private const val HOURS_PER_MINUTE = 60.0
    private val timeRegex = Regex("^([01]\\d|2[0-3]):([0-5]\\d)$")

    /**
     * Calculates hours from a start and finish time in HH:mm format.
     * Returns null when the input is invalid or the finish time is not after the start time.
     */
    fun calculateHoursWorked(startTime: String, finishTime: String): Double? {
        val startMinutes = parseTimeToMinutes(startTime) ?: return null
        val finishMinutes = parseTimeToMinutes(finishTime) ?: return null

        if (finishMinutes <= startMinutes) {
            return null
        }

        return (finishMinutes - startMinutes) / HOURS_PER_MINUTE
    }

    fun validateWorkEntry(
        workDate: String,
        workerName: String,
        startTime: String,
        finishTime: String
    ): String? {
        return when {
            workDate.isBlank() -> "Work date is required."
            !isValidDate(workDate) -> "Use date format dd-MM-yyyy."
            workerName.isBlank() -> "Worker name is required."
            startTime.isBlank() -> "Start time is required."
            !isValidTime(startTime) -> "Use start time format HH:mm."
            finishTime.isBlank() -> "Finish time is required."
            !isValidTime(finishTime) -> "Use finish time format HH:mm."
            calculateHoursWorked(startTime, finishTime) == null -> "Finish time must be after start time."
            else -> null
        }
    }

    fun isValidDate(date: String): Boolean = DateFormatUtils.isValidDisplayDate(date)

    fun isValidTime(time: String): Boolean = timeRegex.matches(time)

    fun formatHours(hours: Double): String {
        return if (hours == hours.toLong().toDouble()) {
            String.format(Locale.getDefault(), "%d", hours.toLong())
        } else {
            String.format(Locale.getDefault(), "%.1f", hours)
        }
    }

    private fun parseTimeToMinutes(value: String): Int? {
        if (!isValidTime(value)) {
            return null
        }

        val (hours, minutes) = value.split(':')
        return hours.toInt() * 60 + minutes.toInt()
    }
}


