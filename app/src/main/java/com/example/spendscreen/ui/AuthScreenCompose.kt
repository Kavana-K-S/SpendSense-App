package com.example.spendscreen.ui

import android.content.Intent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.spendscreen.R


private val Blue = Color(0xFF1976D2)
private val LightBlue = Color(0xFFE3F2FD)

@Composable
fun AuthScreenCompose() {
    val vm = remember { AuthViewModel() }
    var isLogin by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(30.dp))

        // Logo
        // Logo
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.White, shape = RoundedCornerShape(50))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.wallet_logo),
                contentDescription = "Wallet Logo",
                modifier = Modifier.fillMaxSize()
            )
        }




        Spacer(Modifier.height(20.dp))

        // ✅ TOGGLE BUTTONS (PASTE HERE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(LightBlue, RoundedCornerShape(30.dp))
                .padding(4.dp)
        ) {
            ToggleButton(
                text = "Login",
                selected = isLogin,
                onClick = { isLogin = true },
                modifier = Modifier.weight(1f)
            )

            ToggleButton(
                text = "Sign Up",
                selected = !isLogin,
                onClick = { isLogin = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(30.dp))

        if (isLogin) {
            LoginUI(vm, onSignupClick = { isLogin = false })
        } else {
            SignUpUI(vm, onLoginClick = { isLogin = true })
        }
    }
}


@Composable
fun ToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier, // ✅ no weight here
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Blue else Color.Transparent,
            contentColor = if (selected) Color.White else Blue
        ),
        shape = RoundedCornerShape(30.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(text)
    }
}


/* ---------------- TAB BUTTON ---------------- */
@Composable
fun TabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                Color(0xFF1976D2)
            else
                Color.LightGray
        )
    ) {
        Text(
            text = title,
            color = if (selected) Color.White else Color.Black
        )
    }
}
