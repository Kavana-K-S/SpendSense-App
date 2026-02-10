package com.example.spendscreen.ui

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class DateFilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DateFilterScreen(
                onApply = { selectedDate ->
                    intent.putExtra("selectedDate", selectedDate)
                    setResult(RESULT_OK, intent)
                    finish()
                },
                onBack = { finish() }
            )
        }
    }
}

@Composable
fun DateFilterScreen(
    onApply: (Long?) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter by Date") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /* DATE PICKER BUTTON */
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val cal = Calendar.getInstance()
                    DatePickerDialog(
                        ctx,
                        { _, y, m, d ->
                            Calendar.getInstance().apply {
                                set(y, m, d, 0, 0, 0)
                                selectedDate = timeInMillis
                            }
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Icon(Icons.Default.DateRange, null)
                Spacer(Modifier.width(8.dp))
                Text("Select Date")
            }

            /* SELECTED DATE DISPLAY */
            if (selectedDate != null) {
                val formatted = SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                ).format(Date(selectedDate!!))

                Text(
                    text = "Selected: $formatted",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.weight(1f))

            /* APPLY BUTTON */
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(selectedDate) }
            ) {
                Text("Apply")
            }
        }
    }
}
