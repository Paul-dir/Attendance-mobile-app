package com.example.edutrackpro.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Attendance::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
}