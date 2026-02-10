package com.example.spendscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.NotificationEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.data.NotificationRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var notifications by remember { mutableStateOf<List<NotificationEntity>>(emptyList()) }

    // LOAD FROM ROOM
    LaunchedEffect(Unit) {
        notifications = AppDatabase.getDatabase(context)
            .notificationDao()
            .getAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = {
                            scope.launch {
                                AppDatabase.getDatabase(context)
                                    .notificationDao()
                                    .clearAll()
                                notifications = emptyList()
                            }
                        }) {
                            Text("Clear All")
                        }
                    }
                }
            )
        }
    ) { padding ->

        if (notifications.isEmpty()) {
            EmptyNotificationUI(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notifications) { n ->
                    NotificationCard(n)
                }
            }
        }
    }
}


/* ---------------- EMPTY STATE ---------------- */

@Composable
private fun EmptyNotificationUI(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text("No notifications yet", color = Color.Gray)
    }
}

/* ---------------- CARD ---------------- */

@Composable
private fun NotificationCard(n: NotificationEntity) {

    val accent = if (n.type == "WARNING") Color(0xFFD32F2F)
    else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                if (n.type == "WARNING") Icons.Default.Warning
                else Icons.Default.Notifications,
                contentDescription = null,
                tint = accent
            )

            Spacer(Modifier.width(12.dp))

            Column {
                Text(n.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(n.message, fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                Text(formatTime(n.timestamp), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

private fun formatTime(ts: Long): String {
    return SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        .format(Date(ts))
}
