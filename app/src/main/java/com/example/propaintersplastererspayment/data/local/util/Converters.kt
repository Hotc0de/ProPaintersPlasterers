package com.example.propaintersplastererspayment.data.local.util

import androidx.room.TypeConverter
import com.example.propaintersplastererspayment.data.local.entity.JobStatus

class Converters {
    @TypeConverter
    fun fromJobStatus(status: JobStatus): String {
        return status.name
    }

    @TypeConverter
    fun toJobStatus(status: String): JobStatus {
        return JobStatus.valueOf(status)
    }
}
