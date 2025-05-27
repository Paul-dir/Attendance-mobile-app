package com.example.edutrackpro

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, onRoleSelected: (String) -> Unit) {
    var role by remember { mutableStateOf("") }
    var roleExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Select Role",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = roleExpanded,
            onExpandedChange = { roleExpanded = it }
        ) {
            TextField(
                value = role,
                onValueChange = {},
                readOnly = true,
                label = { Text("Role") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = roleExpanded,
                onDismissRequest = { roleExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Teacher") },
                    onClick = {
                        role = "Teacher"
                        roleExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Student") },
                    onClick = {
                        role = "Student"
                        roleExpanded = false
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (role.isNotEmpty()) {
                    onRoleSelected(role)
                    navController.navigate(if (role == "Teacher") "teacher" else "student")
                }
            },
            enabled = role.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}