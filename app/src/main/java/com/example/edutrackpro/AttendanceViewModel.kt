package com.example.edutrackpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrackpro.data.Attendance
import com.example.edutrackpro.data.AttendanceDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AttendanceViewModel(private val dao: AttendanceDao) : ViewModel() {
    val attendanceRecords: Flow<List<Attendance>> = dao.getAllAttendance()

    fun insertAttendance(attendance: Attendance) {
        viewModelScope.launch {
            dao.insert(attendance)
        }
    }
}

