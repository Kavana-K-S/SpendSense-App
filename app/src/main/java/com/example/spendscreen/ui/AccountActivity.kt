package com.example.spendscreen.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.AuthViewModel




class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AuthScreenCompose() // ONLY ONE EXISTS
            }
        }
    }
}


/* ---------------- COLORS ---------------- */
private val BluePrimary = Color(0xFF1976D2)
private val BlueDark = Color(0xFF0D47A1)



/* ---------------- SIGN UP ---------------- */
@Composable
fun SignUpUI(vm: AuthViewModel, onLoginClick: () -> Unit) {
    val ctx = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {



        Text("Create your account", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        if (error.isNotEmpty()) {
            Text(error, color = Color.Red)
        }

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            onClick = {
                if (password != confirm) {
                    error = "Passwords do not match"
                    return@Button
                }

                vm.signUpEmail(
                    email = email,
                    password = password,
                    fullName = fullName
                ) { ok, msg ->
                    if (!ok) {
                        error = msg ?: "Signup failed"
                    } else
                    ctx.startActivity(
                        Intent(ctx, DashboardActivity::class.java)
                    )
                    (ctx as? android.app.Activity)?.finish()

                }
            }
        ) {
            Text("Create Account", color = Color.White)
        }


        Text(
            "Already have an account? Login",
            modifier = Modifier.clickable { onLoginClick() },
            color = BluePrimary,
            fontSize = 14.sp
        )

    }
}



/* ---------------- LOGIN ---------------- */
@Composable
fun LoginUI(vm: AuthViewModel, onSignupClick: () -> Unit) {
    val ctx = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {


        Text("Welcome Back", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPass = !showPass }) {
                    Icon(
                        if (showPass) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error,
                color = Color.Red,
                fontSize = 13.sp
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(
            "Forgot Password?",
            modifier = Modifier
                .align(Alignment.End)
                .clickable {
                    ctx.startActivity(
                        Intent(ctx, ResetPasswordActivity::class.java)
                            .putExtra("email", email)
                    )
                },
            color = BluePrimary,
            fontSize = 13.sp
        )

        Button(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            onClick = {
                vm.loginEmail(
                    email = email,
                    password = password
                ) { ok, msg ->
                    if (!ok) {
                        error = when {
                            msg?.contains("password", true) == true ||
                                    msg?.contains("credential", true) == true ->
                                "Incorrect password"

                            msg?.contains("no user", true) == true ||
                                    msg?.contains("not found", true) == true ->
                                "No account found with this email"

                            msg?.contains("network", true) == true ->
                                "Network error. Please try again"

                            else ->
                                "Login failed. Please try again"
                        }
                    } else {
                        val uid = com.google.firebase.auth.FirebaseAuth
                            .getInstance()
                            .currentUser
                            ?.uid

                        if (uid != null) {
                            com.example.data.SessionManager(ctx).saveUserId(uid)
                        }

                        ctx.startActivity(Intent(ctx, DashboardActivity::class.java))
                        (ctx as android.app.Activity).finish()
                    }
                }
            }
        ) {
            Text("Login", color = Color.White)
        }


        Text(
            "Don’t have an account? Sign Up",
            modifier = Modifier.clickable { onSignupClick() },
            color = BluePrimary,
            fontSize = 14.sp
        )

    }
}


