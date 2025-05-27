package com.example.edutrackpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.edutrackpro.data.AppDatabase
import com.example.edutrackpro.ui.theme.EduTrackProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EduTrackProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "edutrackpro-db"
        ).build()
    }
    val viewModel: AttendanceViewModel = viewModel(
        factory = viewModelFactory { AttendanceViewModel(db.attendanceDao()) }
    )
    var userRole by remember { mutableStateOf("") }

    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController) { role -> userRole = role }
        }
        composable("teacher") {
            if (userRole == "Teacher") {
                TeacherScreen(viewModel, navController)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login")
                }
            }
        }
        composable("student") {
            StudentScreen(viewModel)
        }
        composable("view_attendance") {
            if (userRole == "Teacher") {
                ViewAttendanceScreen(viewModel)
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate("login")
                }
            }
        }
    }
}