package com.example.spendscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutAppScreen(onBack: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About SpendSense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = "SpendSense",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Version 1.0.0",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Divider()

            Text(
                text = "SpendSense is a smart expense tracking app designed to help users manage budgets, track spending habits, and receive timely alerts when limits are reached.",
                fontSize = 15.sp
            )

            Text(
                text = "Key Features:",
                fontWeight = FontWeight.SemiBold
            )

            Text("• Track income & expenses")
            Text("• Category-wise budgeting")
            Text("• Budget alerts & notifications")
            Text("• Analytics & reports")
            Text("• Dark mode support")

            Spacer(Modifier.height(12.dp))

            Text(
                text = "© 2025 SpendSense",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
