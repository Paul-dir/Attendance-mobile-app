package com.example.edutrackpro

import android.os.Bundle
import android.util.Log
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
        try {
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "edutrackpro-db"
            ).build()
        } catch (e: Exception) {
            Log.e("MainActivity", "Room database build error: ${e.message}")
            throw e // Replace with user feedback in production
        }
    }
    val viewModel: AttendanceViewModel = viewModel(
        factory = viewModelFactory { AttendanceViewModel(db.attendanceDao()) }
    )
    var userRole by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        Log.d("AppNavigation", "Navigating to login_screen")
    }

    NavHost(navController = navController, startDestination = "login_screen") {
        composable("login_screen") {
            LoginScreen(
                navController = navController,
                onRoleSelected = { role ->
                    userRole = role
                    Log.d("AppNavigation", "Role selected: $role")
                }
            )
        }
        composable("teacher") {
            if (userRole == "Teacher") {
                TeacherScreen(viewModel, navController)
            } else {
                LaunchedEffect(Unit) {
                    Log.w("AppNavigation", "Unauthorized access to teacher, redirecting")
                    navController.navigate("login_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            }
        }
        composable("navigator") {
            if (userRole == "Student") {
                StudentScreen(viewModel)
            } else {
                LaunchedEffect(Unit) {
                    Log.w("AppNavigation", "Unauthorized access to navigator, redirecting")
                    navController.navigate("login_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            }
        }
        composable("view_attendance") {
            if (userRole == "Teacher") {
                ViewAttendanceScreen(viewModel)
            } else {
                LaunchedEffect(Unit) {
                    Log.w("AppNavigation", "Unauthorized access to view_attendance, redirecting")
                    navController.navigate("login_screen") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            }
        }
    }
}