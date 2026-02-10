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

class CategoryFilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Read current selection from intent (default = All)
        val currentCategory =
            intent.getStringExtra("category") ?: "select category"

        setContent {
            CategoryFilterScreen(
                selectedCategory = currentCategory,
                onCategorySelected = { category ->
                    val result = Intent().apply {
                        putExtra("category", category)
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
fun CategoryFilterScreen(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val ctx = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var current by remember { mutableStateOf(selectedCategory) }

    val categories = listOf(
        "📁 All" to "All",
        "🏠 Rent" to "Rent",
        "💡 Bills & Utilities" to "Bills & Utilities",
        "🛍️ Shopping" to "Shopping",
        "🍔 Food & Dining" to "Food & Dining",
        "🚗 Transportation" to "Transportation",
        "🎓 Education" to "Education",
        "🏥 Healthcare" to "Healthcare",
        "📦 Other" to "Other"
    )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Category") },
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
                    label = { Text("Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                    },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )


                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach { (label, value) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                current = label
                                expanded = false
                                onCategorySelected(value) // ✅ SEND CLEAN VALUE
                            }
                        )
                    }

                }
            }
        }
    }
}
