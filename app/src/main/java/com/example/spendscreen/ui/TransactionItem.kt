package com.example.spendscreen.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(sdf.format(Date(tx.dateMillis)))
            Text(
                text = tx.category?.ifBlank { "—" } ?: "—",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (tx.type == "income") "Income" else "Expense",
                color = if (tx.type == "income") Color(0xFF2E7D32) else Color.Red
            )
            Text(
                text = "₹ ${"%.2f".format(tx.amount)}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
