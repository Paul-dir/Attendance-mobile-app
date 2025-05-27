package com.example.edutrackpro

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.edutrackpro.data.Attendance
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(viewModel: AttendanceViewModel, navController: NavController) {
    var studentName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrText by remember { mutableStateOf("") }
    var hasAttemptedSubmit by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    var batchExpanded by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var teacherExpanded by remember { mutableStateOf(false) }
    var dateExpanded by remember { mutableStateOf(false) }

    val batchOptions = listOf("Batch 2023", "Batch 2024", "Batch 2025")
    val courseOptions = listOf("Mathematics", "Physics", "Computer Science")
    val teacherOptions = listOf("Prof. Smith", "Dr. Jones", "Ms. Brown")
    val dateOptions = remember {
        val calendar = Calendar.getInstance()
        (0..6).map { i ->
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + i)
            String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            ).also { calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - i) }
        }
    }

    val context = LocalContext.current
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            toastMessage = null
        }
    }

    fun generateQrCode(text: String, size: Int = 512): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun copyQrText(): Boolean {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        return if (qrText.isNotEmpty()) {
            val clip = ClipData.newPlainText("QR Code Text", qrText)
            clipboard.setPrimaryClip(clip)
            true
        } else {
            false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "EduTrackPro - Teacher",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        TextField(
            value = studentName,
            onValueChange = { studentName = it },
            label = { Text("Student Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = hasAttemptedSubmit && studentName.isEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = studentId,
            onValueChange = { studentId = it },
            label = { Text("Student ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = hasAttemptedSubmit && studentId.isEmpty()
        )
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = batchExpanded,
            onExpandedChange = { batchExpanded = !batchExpanded }
        ) {
            TextField(
                value = batch,
                onValueChange = {},
                readOnly = true,
                label = { Text("Batch") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = hasAttemptedSubmit && batch.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = batchExpanded,
                onDismissRequest = { batchExpanded = false }
            ) {
                batchOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            batch = option
                            batchExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = dateExpanded,
            onExpandedChange = { dateExpanded = !dateExpanded }
        ) {
            TextField(
                value = date,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = hasAttemptedSubmit && date.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = dateExpanded,
                onDismissRequest = { dateExpanded = false }
            ) {
                dateOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            date = option
                            dateExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = courseExpanded,
            onExpandedChange = { courseExpanded = !courseExpanded }
        ) {
            TextField(
                value = course,
                onValueChange = {},
                readOnly = true,
                label = { Text("Course") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = hasAttemptedSubmit && course.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = courseExpanded,
                onDismissRequest = { courseExpanded = false }
            ) {
                courseOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            course = option
                            courseExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = teacherExpanded,
            onExpandedChange = { teacherExpanded = !teacherExpanded }
        ) {
            TextField(
                value = teacher,
                onValueChange = {},
                readOnly = true,
                label = { Text("Teacher") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                isError = hasAttemptedSubmit && teacher.isEmpty()
            )
            ExposedDropdownMenu(
                expanded = teacherExpanded,
                onDismissRequest = { teacherExpanded = false }
            ) {
                teacherOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            teacher = option
                            teacherExpanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (course.isNotEmpty() && date.isNotEmpty() && teacher.isNotEmpty()) {
                        val text = "Course:$course,Date:$date,Teacher:$teacher"
                        qrText = text
                        qrBitmap = generateQrCode(text)
                        toastMessage = if (qrBitmap != null) {
                            "QR code generated"
                        } else {
                            "Failed to generate QR code"
                        }
                    } else {
                        hasAttemptedSubmit = true
                        val missingFields = mutableListOf<String>()
                        if (course.isEmpty()) missingFields.add("Course")
                        if (date.isEmpty()) missingFields.add("Date")
                        if (teacher.isEmpty()) missingFields.add("Teacher")
                        toastMessage = "Please fill: ${missingFields.joinToString(", ")}"
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            ) {
                Text("Generate Class QR Code")
            }
            Button(
                onClick = {
                    hasAttemptedSubmit = true
                    if (studentName.isNotEmpty() && studentId.isNotEmpty() && batch.isNotEmpty() &&
                        date.isNotEmpty() && course.isNotEmpty() && teacher.isNotEmpty()
                    ) {
                        viewModel.insertAttendance(
                            Attendance(
                                studentName = studentName,
                                studentId = studentId,
                                batch = batch,
                                course = course,
                                date = date,
                                teacher = teacher
                            )
                        )
                        studentName = ""
                        studentId = ""
                        batch = ""
                        date = ""
                        course = ""
                        teacher = ""
                        toastMessage = "Attendance marked"
                    } else {
                        toastMessage = "Please fill all fields"
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                enabled = studentName.isNotEmpty() && studentId.isNotEmpty() && batch.isNotEmpty() &&
                        date.isNotEmpty() && course.isNotEmpty() && teacher.isNotEmpty()
            ) {
                Text("Mark Attendance")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("view_attendance") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Attendance Records")
        }
        qrBitmap?.let { bitmap ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Class QR Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Generated QR Code",
                modifier = Modifier
                    .size(200.dp)
                    .clickable {
                        toastMessage = if (copyQrText()) {
                            "QR code text copied to clipboard"
                        } else {
                            "No QR code generated"
                        }
                    }
            )
        }
    }
}