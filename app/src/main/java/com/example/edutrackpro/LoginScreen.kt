package com.example.edutrackpro

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun LoginScreen(
    navController: NavController,
    onRoleSelected: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("EduTrackPro Login", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Role selection
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    selectedRole = "Student"
                    onRoleSelected("Student")
                    Log.d("LoginScreen", "Student role selected, navigating to navigator")
                    navController.navigate("navigator") {
                        popUpTo("login_screen") { inclusive = true }
                    }
                },
                enabled = selectedRole != "Student"
            ) {
                Text("Student")
            }
            Button(
                onClick = {
                    selectedRole = "Teacher"
                    onRoleSelected("Teacher")
                    Log.d("LoginScreen", "Teacher role selected")
                },
                enabled = selectedRole != "Teacher"
            ) {
                Text("Teacher")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Password for Teacher
        if (selectedRole == "Teacher") {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it.trim() }, // Trim input
                label = { Text("Teacher Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            if (error.isNotEmpty()) {
                Text(error, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    Log.d("LoginScreen", "Password entered: '$password'")
                    if (password == "teacher123") {
                        Log.d("LoginScreen", "Correct password, navigating to teacher")
                        error = ""
                        navController.navigate("teacher") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    } else {
                        error = "Wrong password! Use 'teacher123'."
                        Log.w("LoginScreen", "Incorrect password: '$password'")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotEmpty()
            ) {
                Text("Login as Teacher")
            }
        }
    }
}