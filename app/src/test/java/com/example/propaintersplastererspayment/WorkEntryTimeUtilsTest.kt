package com.example.propaintersplastererspayment

import com.example.propaintersplastererspayment.core.util.WorkEntryTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WorkEntryTimeUtilsTest {

    @Test
    fun calculateHoursWorked_returnsExpectedDecimalHours() {
        val result = WorkEntryTimeUtils.calculateHoursWorked(
            startTime = "08:00",
            finishTime = "16:30"
        )

        assertEquals(8.5, result ?: 0.0, 0.0001)
    }

    @Test
    fun calculateHoursWorked_returnsNull_whenFinishIsBeforeStart() {
        val result = WorkEntryTimeUtils.calculateHoursWorked(
            startTime = "16:30",
            finishTime = "08:00"
        )

        assertNull(result)
    }

    @Test
    fun validateWorkEntry_returnsMessage_whenTimeFormatIsInvalid() {
        val result = WorkEntryTimeUtils.validateWorkEntry(
            workDate = "2026-04-08",
            workerName = "Trung",
            startTime = "8:00",
            finishTime = "16:00"
        )

        assertEquals("Use start time format HH:mm.", result)
    }
}

