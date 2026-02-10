package com.example.spendscreen.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SessionManager
import com.example.data.TransactionEntity
import com.example.viewmodel.AddViewModel
import com.example.viewmodel.SpendViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import androidx.compose.foundation.background
/* ============================================================
   HOME
   ============================================================ */

@Composable
fun HomeScreen(
    addVM: AddViewModel,
    spendVM: SpendViewModel
) {
    val context = LocalContext.current
    val session = SessionManager(context)


    val ctx = LocalContext.current


    LaunchedEffect(Unit) {
        spendVM.loadSummary(ctx)
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {


        Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add Transaction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) {
                    spendVM.loadSummary(ctx)   // 🔥 FORCE REFRESH
                }

                TextButton(onClick = {
                    launcher.launch(
                        Intent(ctx, RecentTransactionsActivity::class.java)
                    )
                }) {
                    Text("Recent Transactions")
                }


            }

            Spacer(Modifier.height(12.dp))

            AddTabs(addVM, spendVM)


            Spacer(Modifier.height(20.dp))

            SummaryRow(spendVM)
        }
    }
}

/* ============================================================
   TABS
   ============================================================ */


@Composable
fun AddTabs(
    addVM: AddViewModel,
    spendVM: SpendViewModel
) {

    var tab by remember { mutableStateOf(0) }

    Row(Modifier.fillMaxWidth()) {
        TabButton("Income", tab == 0, Modifier.weight(1f)) { tab = 0 }
        TabButton("Expense", tab == 1, Modifier.weight(1f)) { tab = 1 }
    }

    Spacer(Modifier.height(16.dp))

    if (tab == 0)
        IncomeForm(addVM, spendVM)
    else
        ExpenseForm(addVM, spendVM)

}

@Composable
fun TabButton(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surfaceVariant,

            contentColor = if (selected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        )

    ) { Text(text) }
}

/* ============================================================
   INCOME
   ============================================================ */

@Composable
fun IncomeForm(
    addVM: AddViewModel,
    spendVM: SpendViewModel
) {

    val ctx = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }

    val cal = Calendar.getInstance()
    val picker = DatePickerDialog(
        ctx,
        { _, y, m, d ->
            cal.set(y, m, d)
            dateText = sdf.format(cal.time)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    Card {
        Column(Modifier.padding(16.dp)) {

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, null,
                        Modifier.clickable { picker.show() })
                },
                readOnly = true
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    amount = ""
                    dateText = ""
                }) {
                    Text("Cancel")
                }

                Button(onClick = {
                    val uid = SessionManager(ctx).getUserId() ?: return@Button

                    addVM.addTransaction(
                        ctx,
                        TransactionEntity(
                            userId = uid,
                            amount = amount.toDouble(),
                            dateMillis = dateText.toDate(),
                            category = "",
                            type = "income",
                            paymentMethod = "Cash",
                            note = ""
                        ),
                        spendVM
                    )

                    amount = ""
                    dateText = ""
                }) {
                    Text("Save")
                }
            }
        }
    }
}

/* ============================================================
   EXPENSE
   ============================================================ */


@Composable
fun ExpenseForm(
    addVM: AddViewModel,
    spendVM: SpendViewModel
) {

    val ctx = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var amount by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Select Category") }
    var payment by remember { mutableStateOf("Select Payment") }
    var note by remember { mutableStateOf("") }

    val cal = Calendar.getInstance()
    val picker = DatePickerDialog(
        ctx,
        { _, y, m, d ->
            cal.set(y, m, d)
            dateText = sdf.format(cal.time)
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )

    Card {
        Column(
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Expense Amount (₹)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = dateText,
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(Icons.Default.DateRange, null,
                        Modifier.clickable { picker.show() })
                },
                readOnly = true
            )

            Spacer(Modifier.height(8.dp))

            DropdownField(
                "Category",
                category,
                listOf(
                    "Select Category", "Rent", "Bills & Utilities",
                    "Shopping", "Food & Dining", "Transportation",
                    "Education", "Healthcare", "Other"
                )
            ) { category = it }

            Spacer(Modifier.height(8.dp))

            DropdownField(
                "Payment Method",
                payment,
                listOf("Select Payment", "Cash", "UPI", "Bank Transfer", "Other")
            ) { payment = it }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Short Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    amount = ""
                    dateText = ""
                    note = ""
                }) {
                    Text("Cancel")
                }

                Button(onClick = {
                    val uid = SessionManager(ctx).getUserId() ?: return@Button

                    addVM.addTransaction(
                        ctx,
                        TransactionEntity(
                            userId = uid,
                            amount = amount.toDouble(),
                            dateMillis = dateText.toDate(),
                            category = category,
                            type = "expense",
                            paymentMethod = payment,
                            note = note
                        ),
                        spendVM
                    )
                }) {
                    Text("Save Expense")
                }
            }
        }
    }
}

/* ============================================================
   SUMMARY
   ============================================================ */

@Composable
fun SummaryRow(spendVM: SpendViewModel) {
    val income by spendVM.totalIncomeState
    val expense by spendVM.totalExpenseState

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SummaryCard("💰 Income", income)
        SummaryCard("💸 Expense", expense)
        SummaryCard("🧮 Remaining", income - expense)
    }
}


@Composable
fun SummaryCard(title: String, amount: Double) {
    Card(
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

    Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text("₹ ${"%.2f".format(amount)}", fontWeight = FontWeight.Bold)
        }
    }
}

/* ============================================================
   DROPDOWN
   ============================================================ */

@Composable
fun DropdownField(
    label: String,
    selected: String,
    items: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = categoryEmojiLabel(selected),
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, null,
                    Modifier.clickable { expanded = true })
            }
        )

        DropdownMenu(expanded, { expanded = false }) {
            items.forEach {
                DropdownMenuItem(
                    text = { Text(categoryEmojiLabel(it)) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }

}

/* ============================================================
   HELPERS
   ============================================================ */

fun categoryEmojiLabel(cat: String): String = when (cat) {
    "Rent" -> "🏠 Rent"
    "Bills & Utilities" -> "💡 Bills & Utilities"
    "Shopping" -> "🛍️ Shopping"
    "Food & Dining" -> "🍔 Food & Dining"
    "Transportation" -> "🚗 Transportation"
    "Education" -> "🎓 Education"
    "Healthcare" -> "🏥 Healthcare"
    "Other" -> "📦 Other"
    "Select Category" -> "📁 Select Category"
    else -> "📁 $cat"
}

fun String.toDate(): Long =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .parse(this)?.time ?: Date().time
