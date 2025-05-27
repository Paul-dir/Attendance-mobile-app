package com.example.edutrackpro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edutrackpro.data.Attendance

@Composable
fun ViewAttendanceScreen(viewModel: AttendanceViewModel) {
    val records by viewModel.attendanceRecords.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Attendance Records",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (records.isEmpty()) {
            Text("No records found")
        } else {
            LazyColumn {
                items(records) { record ->
                    AttendanceItem(record)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AttendanceItem(record: Attendance) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Name: ${record.studentName}", fontSize = 16.sp)
            Text(text = "ID: ${record.studentId}", fontSize = 14.sp)
            Text(text = "Batch: ${record.batch}", fontSize = 14.sp)
            Text(text = "Course: ${record.course}", fontSize = 14.sp)
            Text(text = "Date: ${record.date}", fontSize = 14.sp)
            Text(text = "Teacher: ${record.teacher}", fontSize = 14.sp)
            Text(
                text = "Time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(record.timestamp)}",
                fontSize = 14.sp
            )
        }
    }
}