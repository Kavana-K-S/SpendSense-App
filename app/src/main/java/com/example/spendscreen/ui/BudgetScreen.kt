// ⭐ ONLY ONE NEW FEATURE ADDED = SAFE BANNER (<75%)

package com.example.spendscreen.ui

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import kotlinx.coroutines.launch
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.spendscreen.utils.NotificationHelper
import kotlin.math.max
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.data.CategoryBudgetEntity
import java.util.Calendar
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.data.NotificationRepository

@Composable
fun BudgetScreen(vm: SpendViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // reactive references
    val mainBudget by remember { derivedStateOf { vm.budget.value } }
    val catBudgets by remember { derivedStateOf { vm.categoryBudgets.toList() } }

    // suggested / predicted
    val predictedMonthly = vm.totalExpense().coerceAtLeast(0.0)
    val suggestedMonthly = (predictedMonthly * 1.05).coerceAtLeast(1000.0)

    // Add / Edit dialog controls
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editTarget by remember { mutableStateOf<CategoryBudgetEntity?>(null) }

    // local form state
    var formCategory by remember { mutableStateOf("") }
    var formLimit by remember { mutableStateOf("") }


    var selectedMonth by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    var openHistoryScreen by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<CategoryBudgetEntity?>(null) }



    val monthNames = listOf(
        "January","February","March","April","May","June",
        "July","August","September","October","November","December"
    )

    // categories, icons, colors
    val categories = listOf(
        "Food & Dining", "Transportation", "Healthcare", "Education",
        "Bills & Utilities", "Rent", "Shopping", "Other"
    )



    val catIcons = mapOf(
        "Food & Dining" to "🍔",
        "Transportation" to "🚗",
        "Healthcare" to "🏥",
        "Education" to "🎓",
        "Bills & Utilities" to "💡",
        "Rent" to "🏠",
        "Shopping" to "🛍️",
        "Other" to "📦"
    )

    val categoryColors = mapOf(
        "Food & Dining" to Color(0xFFFF9800),
        "Transportation" to Color(0xFF4CAF50),
        "Healthcare" to Color(0xFFE91E63),
        "Education" to Color(0xFF2196F3),
        "Bills & Utilities" to Color(0xFF9C27B0),
        "Rent" to Color(0xFF3F51B5),
        "Shopping" to Color(0xFFFF5722),
        "Other" to Color(0xFF607D8B)
    )

    // notifications
    val notified = remember { mutableStateListOf<String>() }
    LaunchedEffect(Unit) { NotificationHelper.createChannel(context) }


    // TOP-LEVEL BANNER STATES
    val anyWarning = catBudgets.any {
        val spent = calcSpentFor(vm, it)
        it.limitAmount > 0 && spent / it.limitAmount >= 0.9 && spent / it.limitAmount < 1.0
    }

    val anyExceeded = catBudgets.any {
        val spent = calcSpentFor(vm, it)
        it.limitAmount > 0 && spent / it.limitAmount >= 1.0
    }

    // NEW SAFE CONDITION (< 75%)
    val anySafe = catBudgets.any {
        val spent = calcSpentFor(vm, it)
        it.limitAmount > 0 && (spent / it.limitAmount) < 0.75
    }

    var selectedMonthText by remember {
        mutableStateOf(
            monthNames[Calendar.getInstance().get(Calendar.MONTH)]
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {

        Text("Budget", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        // 🔹 SMART CATEGORY SUGGESTION
        // 🔹 SMART CATEGORY SUGGESTION (FIXED)

        val suggestedCategory by remember(
            vm.transactions.value,
            catBudgets
        ) {
            derivedStateOf {

                val spentMap = vm.transactions.value
                    .filter { it.type == "expense" && !it.category.isNullOrBlank() }
                    .groupBy { it.category!!.trim().lowercase() }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }

                val budgetedSet = catBudgets
                    .map { it.category.trim().lowercase() }
                    .toSet()

                spentMap
                    .filterKeys { it !in budgetedSet }   // 🔥 remove already added categories
                    .toList()
                    .sortedByDescending { it.second }     // 🔥 highest first
                    .firstOrNull()
            }
        }
        val suggestion = suggestedCategory

        if (suggestion != null && suggestion.second > 500) {
            BannerCard(
                title = "💡 Smart Suggestion",
                message = "You spend a lot on ${suggestion.first}. Set a category budget?",
                background = Color(0xFFE3F2FD),
                accent = Color(0xFF1976D2)
            )
            Spacer(Modifier.height(8.dp))
        }




        // 🔹 BUDGET HISTORY
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Budget History", fontWeight = FontWeight.SemiBold)

                    Text(
                        "View",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { openHistoryScreen = true }

                    )
                }

                Spacer(Modifier.height(8.dp))


            }
        }
        Spacer(Modifier.height(12.dp))




        // ---------------- Category Budgets ----------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Category Budgets", fontWeight = FontWeight.SemiBold)

                    Button(onClick = {
                        formCategory = ""
                        formLimit = ""
                        showAddDialog = true
                    }) { Text("Add") }
                }

                Spacer(Modifier.height(8.dp))



                val filteredBudgets = catBudgets.filter {
                    it.month.equals(selectedMonthText, ignoreCase = true) &&
                            it.year == Calendar.getInstance().get(Calendar.YEAR)
                }

                if (filteredBudgets.isEmpty()) {
                    Text("No category budgets for this month")
                } else {
                    filteredBudgets.forEach { cb ->






                    val spent = calcSpentFor(vm, cb)
                        val limit = max(0.0, cb.limitAmount)
                        val usePct = if (limit <= 0.0) 0.0 else (spent / limit * 100.0)

                        val visualFraction = (usePct / 100.0).coerceAtMost(1.0).toFloat()
                        val animProgress by animateFloatAsState(targetValue = visualFraction)

                        val baseColor = categoryColors[cb.category] ?: Color(0xFF4CAF50)
                        val barColor = when {
                            usePct >= 100 -> Color(0xFFD32F2F)
                            usePct >= 90 -> Color(0xFFFFC107)
                            else -> baseColor
                        }

                        val inlineWarning = usePct >= 90 && usePct < 100
                        val inlineExceeded = usePct >= 100
                        val inlineSafe = usePct < 75    // ⭐ NEW SAFE INLINE

                        // notifications
                        LaunchedEffect(usePct, cb.category) {
                            if (usePct >= 90 && !notified.contains(cb.category)) {
                                val isExceeded = usePct >= 100

                                val title =
                                    if (usePct >= 100) "Budget exceeded" else "Budget limit nearing"
                                val body = if (usePct >= 100)
                                    "${cb.category} has exceeded its monthly limit."
                                else
                                    "${cb.category} is ${"%.0f".format(usePct)}% used — nearing limit."

                                Toast.makeText(context, body, Toast.LENGTH_LONG).show()
                                NotificationHelper.notify(
                                    context,
                                    "Budget Alerts",
                                    title,
                                    body,
                                    cb.category.hashCode()
                                )
                                // ✅ SAVE TO ROOM (PERFECT PLACE)
                                NotificationRepository.saveNotification(
                                    context = context,
                                    title = title,
                                    message = body,
                                    type = if (isExceeded) "WARNING" else "INFO"
                                )
                                notified.add(cb.category)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        catIcons[cb.category] ?: "📦",
                                        modifier = Modifier.padding(end = 8.dp),
                                        fontSize = 18.sp
                                    )
                                    Text(cb.category, fontWeight = FontWeight.Medium)
                                }

                                Spacer(Modifier.height(6.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(16.dp)
                                        .background(
                                            Color(0xFFF2F2F2),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(animProgress)
                                            .background(barColor, shape = RoundedCornerShape(8.dp))
                                    )
                                }

                                Spacer(Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Limit: ₹ ${"%.2f".format(cb.limitAmount)} / ${cb.period}",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )

                                    val usedText = "${"%.0f".format(usePct)}% used"
                                    val extra =
                                        if (usePct > 100) " • Exceeded by ₹ ${"%.2f".format(spent - cb.limitAmount)}" else ""
                                    Text("$usedText$extra", fontSize = 12.sp)
                                }

                                Spacer(Modifier.height(6.dp))

                                val remaining = (cb.limitAmount - spent).coerceAtLeast(0.0)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Spent: ₹ ${"%.2f".format(spent)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "Remaining: ₹ ${"%.2f".format(remaining)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // ---------------- INLINE BANNERS ----------------
                                when {
                                    inlineExceeded -> SmallInlineBanner(
                                        text = "🔴 Budget exceeded for ${cb.category}",
                                        color = Color(0xFFFFEBEE),
                                        accent = Color(0xFFD32F2F)
                                    )

                                    inlineWarning -> SmallInlineBanner(
                                        text = "🟡 You're nearing this budget limit",
                                        color = Color(0xFFFFF8E1),
                                        accent = Color(0xFFFFC107)
                                    )

                                    inlineSafe -> SmallInlineBanner(
                                        text = "🟢 Safe — spending is under control",
                                        color = Color(0xFFE8F5E9),
                                        accent = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {

                                // ✏️ EDIT
                                IconButton(onClick = {
                                    editTarget = cb
                                    formCategory = cb.category
                                    formLimit = cb.limitAmount.toString()
                                    showEditDialog = true
                                }) {
                                    Text("✏️")
                                }

                                // 🗑️ DELETE (ask confirmation only)
                                IconButton(onClick = {
                                    showDeleteConfirm = cb
                                }) {
                                    Text("🗑️")
                                }
                            }

                        }

                        Divider()
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }



    // ---------------- ADD DIALOG ----------------
    if (showAddDialog) {
        CategoryBudgetDialog(
            title = "Add Category Budget",
            categories = categories,
            icons = catIcons,
            initialCategory = formCategory,
            initialAmount = formLimit,
            onCancel = { showAddDialog = false },
            onSave = { categoryName, amount ->
                val userId = SessionManager(context).getUserId() ?: "local"
                val cal = Calendar.getInstance()
                val monthName = monthNames[cal.get(Calendar.MONTH)]

                val cb = CategoryBudgetEntity(
                    userId = userId,
                    category = categoryName,
                    limitAmount = amount,
                    period = "Monthly",
                    month = monthName,                // ✅ STRING
                    year = cal.get(Calendar.YEAR)
                )


                scope.launch {
                    vm.addOrUpdateCategoryBudget(context, cb)
                    Toast.makeText(context, "Category saved", Toast.LENGTH_SHORT).show()
                    showAddDialog = false
                }
            }
        )
    }

    // ---------------- EDIT DIALOG ----------------
    if (showEditDialog && editTarget != null) {
        CategoryBudgetDialog(
            title = "Edit Category",
            categories = categories,
            icons = catIcons,
            initialCategory = formCategory,
            initialAmount = formLimit,
            onCancel = { showEditDialog = false; editTarget = null },
            onSave = { newCategory, amount ->
                val oldName = editTarget!!.category
                val updated = editTarget!!.copy(category = newCategory.trim(), limitAmount = amount)

                scope.launch {
                    vm.addOrUpdateCategoryBudget(context, updated)

                    if (newCategory.trim() != oldName) {
                        vm.renameCategoryInTransactions(context, oldName, newCategory.trim())
                    }

                    val spent = calcSpentFor(vm, updated)

                    if (spent >= updated.limitAmount) {
                        NotificationHelper.notify(
                            context,
                            "Budget Alerts",
                            "Budget Exceeded After Edit",
                            "${updated.category} already exceeded by ₹${"%.2f".format(spent - updated.limitAmount)}",
                            updated.category.hashCode()
                        )
                    } else if (spent >= updated.limitAmount * 0.9) {
                        NotificationHelper.notify(
                            context,
                            "Budget Alerts",
                            "Budget Nearing Limit",
                            "${updated.category} is now ${"%.0f".format(spent / updated.limitAmount * 100)}% used",
                            updated.category.hashCode()
                        )
                    }

                    Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                    showEditDialog = false
                    editTarget = null
                }
            }
        )
    }

    if (openHistoryScreen) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Category History") },
                    navigationIcon = {
                        TextButton(onClick = { openHistoryScreen = false }) {
                            Text("Back")
                        }
                    }
                )
            }
        ) { padding ->

            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                // 🔽 Month dropdown (default selected)
                var expanded by remember { mutableStateOf(false) }
                var selectedMonthText by remember { mutableStateOf("Select Month") }
                var isMonthSelected by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = selectedMonthText,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Month") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            null,
                            Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )


                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    monthNames.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                                    onClick = {
                                selectedMonth = index
                                selectedMonthText = monthNames[index]
                                isMonthSelected = true
                                expanded = false
                            }

                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                val historyBudgets = catBudgets.filter {
                    it.month.equals(selectedMonthText, ignoreCase = true) &&
                            it.year == Calendar.getInstance().get(Calendar.YEAR)
                }

                if (!isMonthSelected) {
                    Text(
                        "Please select a month to view category history",
                        color = Color.Gray
                    )
                } else if (historyBudgets.isEmpty()) {
                    Text("No category budgets for this month")
                } else {
                    historyBudgets.forEach { cb ->
                        val spent = calcSpentFor(vm, cb)
                        val remaining = (cb.limitAmount - spent).coerceAtLeast(0.0)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(cb.category, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Limit Amount: ₹ ${"%.2f".format(cb.limitAmount)}")
                                Text("Expense: ₹ ${"%.2f".format(spent)}")
                                Text("Remaining: ₹ ${"%.2f".format(remaining)}")
                            }
                        }
                    }
                }


            }
        }
    }

    if (showDeleteConfirm != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Delete Category Budget") },
            text = { Text("Are you sure you want to delete this category budget?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        vm.deleteCategoryBudget(context, showDeleteConfirm!!)
                        Toast.makeText(context, "Category deleted", Toast.LENGTH_SHORT).show()
                        showDeleteConfirm = null
                    }
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel")
                }
            }
        )
    }

}

/* ----------------- Small Composables ----------------- */

@Composable
private fun BannerCard(title: String, message: String, background: Color, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accent, shape = RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(message, fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun SmallInlineBanner(text: String, color: Color, accent: Color) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = color),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accent, shape = RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(text, fontSize = 13.sp)
            }
        }
    }
}

private val MONTH_NAMES = listOf(
    "January","February","March","April","May","June",
    "July","August","September","October","November","December"
)

@Composable
private fun CategoryBudgetDialog(
    title: String,
    categories: List<String>,
    icons: Map<String, String>,
    initialCategory: String,
    initialAmount: String,
    onCancel: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selCategory by remember {
        mutableStateOf(if (initialCategory.isBlank()) "Select Category" else initialCategory)
    }

    var amountText by remember { mutableStateOf(initialAmount) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Column {

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(icons[c] ?: "📦", modifier = Modifier.padding(end = 8.dp))
                                        Text(c)
                                    }
                                },
                                onClick = {
                                    selCategory = c
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Limit Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amountText.toDoubleOrNull()
                if (selCategory == "Select Category" || amt == null) {
                    Toast.makeText(context, "Please select category and valid amount", Toast.LENGTH_SHORT).show()
                    return@TextButton
                }
                onSave(selCategory.trim(), amt)
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    )
  }

/* ----------------- Spent Calculation ----------------- */




private fun calcSpentFor(
    vm: SpendViewModel,
    cb: CategoryBudgetEntity
): Double {

    return vm.transactions.value
        .filter { it.type == "expense" && !it.category.isNullOrBlank() }
        .filter {
            val cal = Calendar.getInstance().apply {
                timeInMillis = it.dateMillis
            }

            val txMonthName = MONTH_NAMES[cal.get(Calendar.MONTH)]

            txMonthName.equals(cb.month, ignoreCase = true) &&
                    cal.get(Calendar.YEAR) == cb.year
        }
        .filter {
            it.category!!.trim().equals(cb.category, ignoreCase = true)
        }
        .sumOf { it.amount }
}

