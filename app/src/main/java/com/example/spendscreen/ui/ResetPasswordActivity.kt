package com.example.spendscreen.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ResetPasswordScreen(
                defaultEmail = intent.getStringExtra("email") ?: "",
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun ResetPasswordScreen(
    defaultEmail: String,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf(defaultEmail) }
    var loading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Reset Password", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (email.isBlank()) return@Button

                loading = true
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            Toast
                                .makeText(
                                    auth.app.applicationContext,
                                    "Reset email sent. Check your inbox.",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                            onBack()
                        } else {
                            Toast
                                .makeText(
                                    auth.app.applicationContext,
                                    task.exception?.message ?: "Error",
                                    Toast.LENGTH_LONG
                                )
                                .show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(if (loading) "Sending..." else "Send Reset Link")
        }
    }
}
