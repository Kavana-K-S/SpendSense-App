package com.example.spendscreen.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.TransactionEntity
import com.example.viewmodel.SpendViewModel
import com.example.viewmodel.AddViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import com.example.spendscreen.ui.CategoryFilterActivity
import com.example.spendscreen.ui.TypeFilterActivity
import com.example.spendscreen.ui.DateFilterActivity


class RecentTransactionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val spendVM = ViewModelProvider(this)[SpendViewModel::class.java]
        val addVM = ViewModelProvider(this)[AddViewModel::class.java]

        setContent {
            RecentTransactionsScreen(
                spendVM = spendVM,
                addVM = addVM
            )
        }
    }
}


    @Composable
    fun RecentTransactionsScreen(
        spendVM: SpendViewModel,
        addVM: AddViewModel
    ) {

        val ctx = LocalContext.current

    var category by remember { mutableStateOf("All") }
    var type by remember { mutableStateOf("All") }
    var date by remember { mutableStateOf<Long?>(null) }

    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToEdit by remember { mutableStateOf<TransactionEntity?>(null) }


        LaunchedEffect(Unit) {
            addVM.loadTransactions(ctx)
            spendVM.loadSummary(ctx)   // 🔥 IMPORTANT
        }

    /* ✅ SAFE FILTERING */
        val filtered = addVM.transactions.filter { tx ->



        val categoryMatch =
            category == "All" ||
                    tx.category.equals(category, ignoreCase = true)


        val typeMatch =
            type == "All" ||
                    tx.type.trim().equals(type.trim(), ignoreCase = true)

        val dateMatch =
            date == null ||
                    isSameDay(tx.dateMillis, date!!)

        categoryMatch && typeMatch && dateMatch
    }

    val grouped = filtered.groupBy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Date(it.dateMillis))
    }

    /* ✅ SAFE ACTIVITY LAUNCHERS */
    val categoryLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            val result = res.data?.getStringExtra("category")
            if (res.resultCode == Activity.RESULT_OK && !result.isNullOrBlank()) {
                category = result
                type = "All"       // 🔥 RESET
                date = null        // 🔥 RESET
            }
        }


    val typeLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            val result = res.data?.getStringExtra("type")
            if (res.resultCode == Activity.RESULT_OK && !result.isNullOrBlank()) {
                type = result
                category = "All"   // 🔥 RESET
                date = null        // 🔥 RESET
            }
        }


    val dateLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { res ->
            val result = res.data?.getLongExtra("selectedDate", -1L)
            if (res.resultCode == Activity.RESULT_OK && result != null && result != -1L) {
                date = result
                category = "All"   // 🔥 RESET
                type = "All"       // 🔥 RESET
            }
        }




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Transactions") },
                navigationIcon = {
                    IconButton(onClick = { (ctx as ComponentActivity).finish() }) {
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
                .padding(16.dp)
        ) {

            /* FILTER BUTTONS */
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    categoryLauncher.launch(
                        Intent(ctx, CategoryFilterActivity::class.java)
                    )
                }) { Text("Category") }

                Button(onClick = {
                    typeLauncher.launch(
                        Intent(ctx, TypeFilterActivity::class.java)
                    )
                }) { Text("Type") }

                Button(onClick = {
                    dateLauncher.launch(
                        Intent(ctx, DateFilterActivity::class.java)
                    )
                }) { Text("Date") }
            }

            Spacer(Modifier.height(12.dp))
            Divider()

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                if (grouped.isEmpty()) {
                    Text("No transactions found", color = Color.Gray)
                }

                grouped.forEach { (month, list) ->
                    Text(
                        text = month,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    list.forEach { tx ->
                        TransactionRow(
                            tx = tx,
                            onEdit = { txToEdit = tx },
                            onDelete = { txToDelete = tx }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    /* DELETE CONFIRMATION */
    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    addVM.deleteTransaction(
                        ctx,
                        txToDelete!!,
                        spendVM
                    )

                    txToDelete = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    /* EDIT DIALOG */
    if (txToEdit != null) {
        var amount by remember { mutableStateOf(txToEdit!!.amount.toString()) }

        AlertDialog(
            onDismissRequest = { txToEdit = null },
            title = { Text("Edit Transaction") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") }
                )
            },
            confirmButton = {
                TextButton(onClick = {

                    addVM.updateTransaction(
                        ctx,
                        txToEdit!!.copy(amount = amount.toDouble()),
                        spendVM
                    )

                    txToEdit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { txToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ✅ CORRECT TYPE DISPLAY */
@Composable
fun TransactionRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = tx.type.equals("income", true)

    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(tx.category)
            Text(
                text =
                    if (isIncome) "Income ₹ ${tx.amount}"
                    else "Expense ₹ ${tx.amount}",
                color = if (isIncome) Color(0xFF2E7D32) else Color.Red
            )
        }

        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, null)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, null, tint = Color.Red)
        }
    }
}

/* DATE HELPER */
fun isSameDay(t1: Long, t2: Long): Boolean {
    val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
    val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}
