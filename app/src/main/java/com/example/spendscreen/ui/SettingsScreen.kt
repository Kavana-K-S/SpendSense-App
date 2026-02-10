package com.example.spendscreen.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionManager
import com.example.viewmodel.SpendViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.spendscreen.ui.deleteAccountFirebase
import com.example.data.NotificationRepository
import com.example.data.ProfileRepository
import com.example.spendscreen.ui.FAQScreen
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(
    vm: SpendViewModel,
    isDarkTheme: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenFAQ: () -> Unit
    
)
{
    val context = LocalContext.current
    val session = SessionManager(context)
    val uid = session.getUserId()
    var showDeleteDialog by remember { mutableStateOf(false) }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        /* ---------------- PROFILE HEADER ---------------- */

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val auth = FirebaseAuth.getInstance()
                    val email = auth.currentUser?.email ?: "No email linked"

                    Text(
                        text = "Profile", // ✅ ALWAYS SHOW PROFILE
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )


                    Text(
                        text = "User ID: ${uid?.take(8)}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                    Text(
                    "Edit",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        context.startActivity(
                            Intent(context, ViewProfileActivity::class.java)
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------------- GENERAL ---------------- */

        SectionTitle("General")

        SettingsItem(
            icon = Icons.Default.HelpOutline,
            title = "Help Guide",
            onClick = {
                context.startActivity(Intent(context, HelpGuideActivity::class.java))
            }
        )



        Spacer(Modifier.height(24.dp))

        /* ---------------- APPEARANCE ---------------- */

        SectionTitle("Appearance")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DarkMode, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Dark Theme", modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkTheme,
                onCheckedChange = { checked ->
                    session.setDarkTheme(checked)   // save preference
                    onThemeChanged(checked)         // update app theme
                }
            )

        }

        Spacer(Modifier.height(24.dp))

        SettingsItem(
            icon = Icons.Default.Notifications,
            title = "Notifications",
            onClick = onOpenNotifications
        )

        SettingsItem(
            icon = Icons.Default.Info,
            title = "About App",
            onClick = onOpenAbout
        )

        SettingsItem(
            icon = Icons.Default.QuestionAnswer,
            title = "FAQs",
            onClick = onOpenFAQ
        )




        /* ---------------- ACCOUNT ---------------- */

        SectionTitle("Account")

        SettingsItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            titleColor = Color(0xFF6A4BC3),
            onClick = {
                FirebaseAuth.getInstance().signOut()
                session.clear()
                val intent = Intent(context, AccountActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
        )

        SettingsItem(
            icon = Icons.Default.Delete,
            title = "Delete Account",
            titleColor = Color(0xFFD32F2F),
            onClick = { showDeleteDialog = true }
        )
    }

    /* ---------------- DELETE DIALOG ---------------- */

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = {
                Text("This action is permanent and will delete all your data.")
            },
            confirmButton = {
                TextButton(onClick = {
                    deleteAccountFirebase(context, vm, session)
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------------- REUSABLE COMPONENTS ---------------- */

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    titleColor: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            modifier = Modifier.weight(1f),
            color = titleColor
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}
fun deleteAccountFirebase(
    context: Context,
    vm: SpendViewModel,
    session: SessionManager
) {
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    // Clear local DB


    // Firebase delete
    auth.currentUser?.delete()

    // Clear session
    session.clear()

    // Go to login screen
    val intent = Intent(context, AccountActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}
