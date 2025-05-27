package com.example.edutrackpro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val studentId: String,
    val batch: String,
    val course: String,
    val date: String,
    val teacher: String,
    val timestamp: Long = System.currentTimeMillis()
)