package com.example.edutrackpro

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.edutrackpro.data.Attendance
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var studentName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var scannedData by remember { mutableStateOf<Map<String, String>?>(null) }
    var lastScannedData by remember { mutableStateOf<Map<String, String>?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var lastScannedRaw by remember { mutableStateOf<String?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    var manualQrInput by remember { mutableStateOf("") }
    var isManualMode by remember { mutableStateOf(false) }
    val defaultCourse = "Mathematics" // Fallback default
    val defaultTeacher = "Prof. Smith" // Fallback default
    val defaultDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            Log.d("StudentScreen", "Toast: $it")
            toastMessage = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        Log.d("StudentScreen", "Camera permission: $granted")
        toastMessage = if (granted) "Camera enabled" else "Camera permission denied"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "EduTrackPro - Student Attendance",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isManualMode) {
            if (hasCameraPermission) {
                CameraPreview(
                    onBarcodeScanned = { barcode ->
                        barcode.rawValue?.let { raw ->
                            Log.d("StudentScreen", "QR scanned: $raw")
                            lastScannedRaw = raw
                            val data = parseQrCode(raw)
                            if (data != null) {
                                scannedData = data
                                lastScannedData = data
                                toastMessage = "QR scanned successfully"
                                Log.d("StudentScreen", "Form data: Course=${data["Course"]}, Date=${data["Date"]}, Teacher=${data["Teacher"]}")
                            } else {
                                toastMessage = "Invalid QR code format: $raw"
                                Log.w("StudentScreen", "Parse failed for: $raw")
                            }
                        } ?: run {
                            toastMessage = "Empty QR code"
                            Log.w("StudentScreen", "Empty QR scanned")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        toastMessage = lastScannedRaw?.let { "Last QR: $it" } ?: "No QR scanned yet"
                        Log.d("StudentScreen", "Debug QR: $lastScannedRaw")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Show Last Scanned QR")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Or enter QR manually:", fontSize = 14.sp)
                TextField(
                    value = manualQrInput,
                    onValueChange = { manualQrInput = it },
                    label = { Text("QR (e.g., Course:Math,Date:2025-06-12,Teacher:John)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        Log.d("StudentScreen", "Manual QR input: $manualQrInput")
                        val data = parseQrCode(manualQrInput)
                        if (data != null) {
                            scannedData = data
                            lastScannedData = data
                            toastMessage = "Manual QR processed"
                            Log.d("StudentScreen", "Manual form data: Course=${data["Course"]}, Date=${data["Date"]}, Teacher=${data["Teacher"]}")
                        } else {
                            toastMessage = "Invalid manual QR format: $manualQrInput"
                            Log.w("StudentScreen", "Manual parse failed: $manualQrInput")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Process Manual QR")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        isManualMode = true
                        scannedData = null
                        Log.d("StudentScreen", "Switched to manual mode")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fill Form Manually")
                }
            } else {
                Button(
                    onClick = {
                        Log.d("StudentScreen", "Requesting camera permission")
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Camera")
                }
            }
        } else {
            Button(
                onClick = {
                    isManualMode = false
                    studentName = ""
                    studentId = ""
                    batch = ""
                    Log.d("StudentScreen", "Switched to QR mode")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to QR Scanning")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isManualMode) {
            val formCourse = lastScannedData?.get("Course") ?: defaultCourse
            val formDate = lastScannedData?.get("Date") ?: defaultDate
            val formTeacher = lastScannedData?.get("Teacher") ?: defaultTeacher
            LaunchedEffect(Unit) {
                Log.d("StudentScreen", "Manual form: Course=$formCourse, Date=$formDate, Teacher=$formTeacher")
            }
            Text("Manual Attendance Form", fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = formCourse,
                onValueChange = {},
                readOnly = true,
                label = { Text("Course (auto)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = formDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Date (auto)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = formTeacher,
                onValueChange = {},
                readOnly = true,
                label = { Text("Teacher (auto)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = studentName,
                onValueChange = { studentName = it.trim() },
                label = { Text("Student Name (required)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = studentId,
                onValueChange = { studentId = it.trim() },
                label = { Text("Student ID (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = batch,
                onValueChange = { batch = it.trim() },
                label = { Text("Batch (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (studentName.isBlank()) {
                        toastMessage = "Please enter Student Name"
                        Log.d("StudentScreen", "Validation failed: Missing student name")
                        return@Button
                    }
                    scope.launch {
                        try {
                            Log.d("StudentScreen", "Submitting manual: Name=$studentName, ID=$studentId, Batch=$batch, Course=$formCourse, Date=$formDate, Teacher=$formTeacher")
                            viewModel.insertAttendance(
                                Attendance(
                                    studentName = studentName,
                                    studentId = studentId.ifBlank { "N/A" },
                                    batch = batch.ifBlank { "N/A" },
                                    course = formCourse,
                                    date = formDate,
                                    teacher = formTeacher
                                )
                            )
                            toastMessage = "Attendance recorded successfully"
                            Log.d("StudentScreen", "Manual attendance submitted")
                            studentName = ""
                            studentId = ""
                            batch = ""
                            isManualMode = false
                        } catch (e: Exception) {
                            toastMessage = "Submission failed: ${e.message}"
                            Log.e("StudentScreen", "Submission error: ${e.message}", e)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = studentName.isNotBlank()
            ) {
                Text("Submit Attendance")
            }
        } else {
            scannedData?.let { data ->
                LaunchedEffect(Unit) {
                    Log.d("StudentScreen", "QR form: Course=${data["Course"]}, Date=${data["Date"]}, Teacher=${data["Teacher"]}")
                }
                Text("Attendance Form", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = data["Course"] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course (auto)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = data["Date"] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date (auto)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = data["Teacher"] ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Teacher (auto)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = studentName,
                    onValueChange = { studentName = it.trim() },
                    label = { Text("Student Name (required)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = studentId,
                    onValueChange = { studentId = it.trim() },
                    label = { Text("Student ID (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = batch,
                    onValueChange = { batch = it.trim() },
                    label = { Text("Batch (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (studentName.isBlank()) {
                            toastMessage = "Please enter Student Name"
                            Log.d("StudentScreen", "Validation failed: Empty student name")
                            return@Button
                        }
                        scope.launch {
                            try {
                                Log.d("StudentScreen", "Submitting: Name=$studentName, ID=$studentId, Batch=$batch, Course=${data["Course"]}, Date=${data["Date"]}, Teacher=${data["Teacher"]}")
                                viewModel.insertAttendance(
                                    Attendance(
                                        studentName = studentName,
                                        studentId = studentId.ifBlank { "N/A" },
                                        batch = batch.ifBlank { "N/A" },
                                        course = data["Course"] ?: "",
                                        date = data["Date"] ?: "",
                                        teacher = data["Teacher"] ?: ""
                                    )
                                )
                                toastMessage = "Attendance recorded successfully"
                                Log.d("StudentScreen", "Attendance submitted successfully")
                                studentName = ""
                                studentId = ""
                                batch = ""
                                scannedData = null
                            } catch (e: Exception) {
                                toastMessage = "Submission failed: ${e.message}"
                                Log.e("StudentScreen", "Submission error: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = studentName.isNotBlank()
                ) {
                    Text("Submit Attendance")
                }
            } ?: Text("Scan a QR code or fill manually", fontSize = 16.sp)
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeScanned: (Barcode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var isCameraInitialized by remember { mutableStateOf(false) }
    var lastScannedTime by remember { mutableStateOf(0L) }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder()
                            .setTargetResolution(android.util.Size(640, 480))
                            .build()
                            .also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                        val cameraSelector = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                            CameraSelector.DEFAULT_BACK_CAMERA
                        } else {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }

                        val barcodeScanner = BarcodeScanning.getClient(
                            BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                .build()
                        )

                        val analysis = ImageAnalysis.Builder()
                            .setTargetResolution(android.util.Size(640, 480))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analysis.setAnalyzer(executor) { imageProxy ->
                            @OptIn(ExperimentalGetImage::class)
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull()?.let { barcode ->
                                            val currentTime = System.currentTimeMillis()
                                            if (currentTime - lastScannedTime > 2000) {
                                                Log.d("CameraPreview", "Barcode detected: ${barcode.rawValue}")
                                                onBarcodeScanned(barcode)
                                                lastScannedTime = currentTime
                                                analysis.clearAnalyzer()
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("CameraPreview", "Scan error: ${e.message}", e)
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        imageAnalysis = analysis

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, analysis)
                        isCameraInitialized = true
                        Log.d("CameraPreview", "Camera initialized")
                    } catch (e: Exception) {
                        Log.e("CameraPreview", "Camera setup error: ${e.message}", e)
                    }
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isCameraInitialized) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }
    }
}

private fun parseQrCode(raw: String): Map<String, String>? {
    return try {
        Log.d("StudentScreen", "Parsing QR: $raw")
        val parts = raw.split(",").associateNotNull { pairString ->
            val pair = pairString.split(":", limit = 2)
            if (pair.size == 2) {
                val key = pair[0].trim().lowercase()
                val value = pair[1].trim()
                if (key.isNotBlank() && value.isNotBlank()) {
                    when (key) {
                        "course" -> "Course" to value
                        "date" -> "Date" to value
                        "teacher" -> "Teacher" to value
                        else -> null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        }

        val normalizedParts = parts.mapKeys { it.key.lowercase() }
        if (normalizedParts.containsKey("course") && normalizedParts.containsKey("date") && normalizedParts.containsKey("teacher")) {
            Log.d("StudentScreen", "Parsed QR: $parts")
            parts
        } else {
            Log.w("StudentScreen", "Invalid QR: missing required fields (Course, Date, Teacher) in $normalizedParts")
            null
        }
    } catch (e: Exception) {
        Log.e("StudentScreen", "QR parse error: ${e.message}", e)
        null
    }
}

inline fun <T> Iterable<T>.associateNotNull(transform: (T) -> Pair<String, String>?): Map<String, String> {
    val destination = mutableMapOf<String, String>()
    for (element in this) {
        transform(element)?.let { (key, value) ->
            destination[key] = value
        }
    }
    return destination
}