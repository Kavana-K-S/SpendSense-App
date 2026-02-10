package com.example.spendscreen.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class TypeFilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ READ current type (default = Income)
        val currentType =
            intent.getStringExtra("type") ?: "select type"

        setContent {
            TypeFilterScreen(
                selectedType = currentType,
                onTypeSelected = { type ->
                    val result = Intent().apply {
                        putExtra("type", type)
                    }
                    setResult(Activity.RESULT_OK, result)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeFilterScreen(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val ctx = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf(selectedType) }

    // ✅ Only Income & Expense (NO "All" as you requested)
    val types = listOf("Income", "Expense")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Type") },
                navigationIcon = {
                    IconButton(onClick = { (ctx as Activity).finish() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {

                OutlinedTextField(
                    value = current,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    types.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                current = type
                                expanded = false
                                onTypeSelected(type) // ✅ AUTO APPLY
                            }
                        )
                    }
                }
            }
        }
    }
}
