package com.example.edutrackpro

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(viewModel: AttendanceViewModel, navController: NavController) {
    var course by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Auto-set today's date in YYYY-MM-DD format
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val date = dateFormat.format(Date())

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.d("TeacherScreen", "Toast: $it")
            toastMessage = null
        }
    }

    fun generateQrCode(text: String, size: Int = 400): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
                }
            }
            Log.d("TeacherScreen", "QR generated: $text")
            bitmap
        } catch (e: Exception) {
            Log.e("TeacherScreen", "QR generation failed: ${e.message}", e)
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EduTrackPro - Generate Class QR",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        TextField(
            value = course,
            onValueChange = { course = it.trim().replace(",", "").replace(":", "") },
            label = { Text("Course") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = teacher,
            onValueChange = { teacher = it.trim().replace(",", "").replace(":", "") },
            label = { Text("Teacher") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = date,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (course.isBlank() || teacher.isBlank()) {
                    toastMessage = "Please enter Course and Teacher"
                    Log.d("TeacherScreen", "Validation failed: Empty course or teacher")
                    return@Button
                }
                val qrContent = "Course:$course,Date:$date,Teacher:$teacher"
                Log.d("TeacherScreen", "Generating QR: $qrContent")
                qrBitmap = generateQrCode(qrContent)
                toastMessage = if (qrBitmap != null) "QR code generated" else "Failed to generate QR code"
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = course.isNotBlank() && teacher.isNotBlank()
        ) {
            Text("Generate Class QR Code")
        }
        Spacer(modifier = Modifier.height(16.dp))
        qrBitmap?.let { bitmap ->
            Text(
                text = "Class QR Code",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Generated QR Code",
                modifier = Modifier.size(200.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("view_attendance") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Attendance Records")
        }
    }
}