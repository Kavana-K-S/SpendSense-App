package com.example.spendscreen.ui
import android.app.DatePickerDialog
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.viewmodel.SpendViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(vm: SpendViewModel) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    var from by remember { mutableStateOf<String?>(null) }
    var to by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf("Select Category") }
    var results by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

    val categoryItems = listOf(
        "Select Category",
        "📁 All",
        "🏠 Rent",
        "💡 Bills & Utilities",
        "🛍️ Shopping",
        "🍔 Food & Dining",
        "🚗 Transportation",
        "🎓 Education",
        "🩺 Healthcare",
        "📦 Other"
    )


    // MONTH DROPDOWN VALUES
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val cal = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }
    var selectedYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }

    var showHeatmap by remember { mutableStateOf(false) }

    // Bottom sheet state
    var sheetDate by remember { mutableStateOf<Date?>(null) }
    var sheetTransactions by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }
    var showSheet by remember { mutableStateOf(false) }

    var showCsvPreview by remember { mutableStateOf(false) }
    var csvPreviewData by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }


    // DATE PICKERS
    val fromPicker = DatePickerDialog(
        context, { _, y, m, d ->
            val c = Calendar.getInstance()
            c.set(y, m, d)
            from = sdf.format(c.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
    )

    val toPicker = DatePickerDialog(
        context, { _, y, m, d ->
            val c = Calendar.getInstance()
            c.set(y, m, d)
            to = sdf.format(c.time)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
    )

    // DAILY TOTALS FOR HEATMAP
    val dailyTotalsForMonth by remember(
        vm.transactions.value,
        selectedMonth,
        selectedYear
    ) {
        mutableStateOf(
            calcDailyTotals(
                vm.transactions.value,
                selectedYear,
                selectedMonth
            )
        )
    }

    // ✅ MOVED HERE (Fixes CSV & PDF error)
    fun applyFilters(): List<TransactionEntity> {
        val fDate = from?.let { runCatching { sdf.parse(it) }.getOrNull() }
        val tDate = to?.let { runCatching { sdf.parse(it) }.getOrNull() }

        return vm.transactions.value.filter { tx ->
            val d = Date(tx.dateMillis)

            val okFrom = fDate?.let { !d.before(it) } ?: true
            val okTo = tDate?.let { !d.after(it) } ?: true
            val okCat = when {
                category == "Select Category" -> true
                category == "📁 All" || category == "All" -> true
                else -> {
                    val cleanCat = category.substringAfter(" ").trim()
                    tx.category.equals(cleanCat, ignoreCase = true)
                }
            }
            okFrom && okTo && okCat
        }.sortedByDescending { it.dateMillis }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        item {
            Text("Reports", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
        }

        /* ------------------------------ FILTER CARD ------------------------------ */

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(modifier = Modifier.fillMaxWidth()) {

                        OutlinedTextField(
                            value = from ?: "",
                            onValueChange = { from = it.takeIf { it.isNotBlank() } },
                            label = { Text("From") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { fromPicker.show() }) {
                                    Icon(Icons.Default.DateRange, null)
                                }
                            }
                        )

                        Spacer(Modifier.width(8.dp))

                        OutlinedTextField(
                            value = to ?: "",
                            onValueChange = { to = it.takeIf { it.isNotBlank() } },
                            label = { Text("To") },
                            modifier = Modifier.weight(1f),
                            trailingIcon = {
                                IconButton(onClick = { toPicker.show() }) {
                                    Icon(Icons.Default.DateRange, null)
                                }
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Category dropdown
                    // ---------------- CATEGORY DROPDOWN ----------------
                    var expanded by remember { mutableStateOf(false) }

// New list with "All" added for filtering
                    val dropdownItems = listOf("All") + categoryItems

                    Box(modifier = Modifier.fillMaxWidth()) {

                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { expanded = true }
                                )
                            }
                        )

                        // Entire text field clickable
                        Spacer(
                            Modifier
                                .matchParentSize()
                                .clickable { expanded = true }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {

                            dropdownItems.forEach { item ->

                                DropdownMenuItem(
                                    text = { Text(item, fontSize = 16.sp) },
                                    onClick = {
                                        category = item
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }




                    Spacer(Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {


                        Button(onClick = {
                            results = applyFilters()
                        }) {
                            Text("Generate")
                        }


                        Spacer(Modifier.width(10.dp))

                        Button(onClick = {
                            val filtered = applyFilters()
                            results = filtered   // show preview on screen

                            if (filtered.isEmpty()) {
                                android.widget.Toast.makeText(
                                    context,
                                    "No data to export",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val uri = vm.exportCsv(context, filtered)
                            android.util.Log.d("CSV_EXPORT", "URI = $uri")
                            if (uri == null) {
                                android.widget.Toast.makeText(
                                    context,
                                    "Export failed",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/csv"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            val chooser = Intent.createChooser(intent, "Share CSV")
                            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(chooser)

                            context.startActivity(Intent.createChooser(intent, "Share CSV"))
                            csvPreviewData = filtered
                            showCsvPreview = true
                        }) { Text("CSV") }



                        if (showCsvPreview) {
                            AlertDialog(
                                onDismissRequest = { showCsvPreview = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val uri = vm.exportCsv(context, csvPreviewData)
                                        if (uri != null) {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/csv"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(
                                                    intent,
                                                    "Share CSV"
                                                )
                                            )
                                        }
                                        showCsvPreview = false
                                    }) { Text("Share CSV") }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        showCsvPreview = false
                                    }) { Text("Cancel") }
                                },
                                title = { Text("CSV Preview") },
                                text = {
                                    LazyColumn(modifier = Modifier.height(300.dp)) {
                                        items(csvPreviewData) { tx ->
                                            Text("${SimpleDateFormat("yyyy-MM-dd").format(Date(tx.dateMillis))} | ${tx.category} | ${tx.type} | ₹${tx.amount}")
                                        }
                                    }
                                }
                            )
                        }

                    }
                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val filtered = applyFilters()
                            results = filtered

                            if (filtered.isEmpty()) {
                                android.widget.Toast.makeText(context, "No data to export", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val uri = vm.exportPdf(context, filtered)
                            android.util.Log.d("PDF_EXPORT", "PDF URI = $uri")  // ✅ log AFTER creating uri

                            if (uri == null) {
                                android.widget.Toast.makeText(context, "PDF export failed", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }

                            context.startActivity(Intent.createChooser(intent, "Share PDF"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Export PDF") }


                }
            }

            Spacer(Modifier.height(20.dp))
        }

        /* ------------------------------ HEATMAP BUTTON ------------------------------ */

        item {
            Button(
                onClick = { showHeatmap = !showHeatmap },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF6A1B9A))
            ) {
                Text(if (showHeatmap) "Hide Heatmap" else "Show Heatmap")
            }
            Spacer(Modifier.height(12.dp))
        }



        /* ------------------------------ MONTH + YEAR DROPDOWNS ------------------------------ */
        if (showHeatmap) {
            item {

                var monthExpanded by remember { mutableStateOf(false) }
                var yearExpanded by remember { mutableStateOf(false) }

                val yearList = (2020..2035).toList()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    /* ----- MONTH DROPDOWN ----- */
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = monthNames[selectedMonth],
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Month") },
                            trailingIcon = {
                                IconButton({ monthExpanded = true }) {
                                    Icon(Icons.Default.DateRange, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            monthNames.forEachIndexed { i, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        selectedMonth = i
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    /* ----- YEAR DROPDOWN ----- */
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = {
                                IconButton({ yearExpanded = true }) {
                                    Icon(Icons.Default.DateRange, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            yearList.forEach { y ->
                                DropdownMenuItem(
                                    text = { Text(y.toString()) },
                                    onClick = {
                                        selectedYear = y
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }
        }


        /* ------------------------------ HEATMAP GRID ------------------------------ */

        if (showHeatmap) {
            item {
                MonthlyHeatmap(
                    year = selectedYear,
                    month = selectedMonth,
                    dailyTotals = dailyTotalsForMonth,
                    allTransactions = vm.transactions.value,
                    onDayClick = { date, txs ->
                        sheetDate = date
                        sheetTransactions = txs
                        showSheet = true
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }

        /* ------------------------------ RESULT LIST ------------------------------ */

        items(results) { tx ->
            val dFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(dFormat.format(Date(tx.dateMillis)))
                    Text(tx.category ?: "", color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(if (tx.type == "income") "Income" else "Expense")
                    Text("₹ ${tx.amount}")
                }
            }
            Divider()
        }
    }

    /* ------------------------------ BOTTOM SHEET ------------------------------ */

    if (showSheet && sheetDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Text(df.format(sheetDate!!), fontWeight = FontWeight.Bold)
                val totalIncome = sheetTransactions.filter { it.type == "income" }.sumOf { it.amount }
                val totalExpense = sheetTransactions.filter { it.type == "expense" }.sumOf { it.amount }

                val diffColor = when {
                    totalExpense > totalIncome -> Color(0xFFD32F2F)   // RED
                    totalExpense < totalIncome -> Color(0xFF4CAF50)   // GREEN
                    kotlin.math.abs(totalExpense - totalIncome) <= totalIncome * 0.20 ->
                        Color(0xFFFFC107)                             // YELLOW
                    else -> Color(0xFFFFC107)
                }

                Text("Income: ₹$totalIncome", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                Text("Expense: ₹$totalExpense", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                Text("Difference: ₹${totalIncome - totalExpense}", color = diffColor, fontWeight = FontWeight.Bold)
                Text("${sheetTransactions.size} transactions", color = Color.Gray)


                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                if (sheetTransactions.isEmpty()) {
                    Text("No transactions for this day.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                        items(sheetTransactions) { tx ->
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Text(tx.category ?: "—", fontWeight = FontWeight.Bold)
                                Text("₹ ${tx.amount}")
                            }
                            Divider()
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

/* ------------------------------ HEATMAP COMPOSABLE ------------------------------ */

@Composable
private fun MonthlyHeatmap(
    year: Int,
    month: Int,
    dailyTotals: Map<Int, Double>,
    allTransactions: List<TransactionEntity>,
    onDayClick: (Date, List<TransactionEntity>) -> Unit
) {
    val cal = Calendar.getInstance()
    cal.set(year, month, 1)

    val firstWeekday = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Square box size
    val cellSize = 70.dp

    val cells = mutableListOf<Int?>()
    repeat(firstWeekday - Calendar.SUNDAY) { cells.add(null) }
    (1..daysInMonth).forEach { cells.add(it) }
    while (cells.size % 7 != 0) cells.add(null)

    Column {

        // Week headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        cells.chunked(7).forEach { week ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                week.forEach { day ->

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .height(cellSize)
                    ) {

                        if (day == null) {
                            Box(modifier = Modifier.height(cellSize))
                        } else {

                            // Fetch income + expense for the selected day
                            val dayTx = allTransactions.filter { tx ->
                                val c = Calendar.getInstance()
                                c.time = Date(tx.dateMillis)
                                c.get(Calendar.YEAR) == year &&
                                        c.get(Calendar.MONTH) == month &&
                                        c.get(Calendar.DAY_OF_MONTH) == day
                            }

                            val totalExpense = dayTx.filter { it.type == "expense" }.sumOf { it.amount }
                            val totalIncome  = dayTx.filter { it.type == "income" }.sumOf { it.amount }

                            val color = when {

                                // 0% → No income AND no expense → Grey
                                totalIncome == 0.0 && totalExpense == 0.0 ->
                                    Color(0xFFE0E0E0)   // Grey

                                // No income but expense exists → 100%+ overspending
                                totalIncome == 0.0 && totalExpense > 0.0 ->
                                    Color(0xFFE57373)   // Red

                                // >= 100%
                                totalExpense >= totalIncome ->
                                    Color(0xFFE57373)   // Red

                                // 90% – < 100%
                                totalExpense >= totalIncome * 0.90 ->
                                    Color(0xFFFFB74D)   // Orange

                                // 75% – < 90%
                                totalExpense >= totalIncome * 0.75 ->
                                    Color(0xFFFFF176)   // Yellow

                                // 50% – < 75%
                                totalExpense >= totalIncome * 0.50 ->
                                    Color(0xFF388E3C)   // Dark Green

                                // 25% – < 50%
                                totalExpense >= totalIncome * 0.25 ->
                                    Color(0xFF66BB6A)   // Medium Green

                                // 1% – < 25%
                                totalExpense >= totalIncome * 0.01 ->
                                    Color(0xFF81C784)   // Light Green

                                // Fallback
                                else ->
                                    Color(0xFFE0E0E0)   // Grey
                            }



                            Column(
                                modifier = Modifier
                                    .size(cellSize)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .clickable {

                                        val cc = Calendar.getInstance()
                                        cc.set(year, month, day, 0, 0, 0)

                                        onDayClick(cc.time, dayTx)
                                    }
                                    .padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    day.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )


                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}


/* ------------------------------ HELPERS ------------------------------ */

private fun calcDailyTotals(
    transactions: List<TransactionEntity>,
    year: Int,
    month: Int
): Map<Int, Double> {
    val cal = Calendar.getInstance()
    val map = mutableMapOf<Int, Double>()

    transactions.forEach { tx ->
        cal.time = Date(tx.dateMillis)

        if (cal.get(Calendar.YEAR) == year &&
            cal.get(Calendar.MONTH) == month &&
            tx.type == "expense"
        ) {
            val d = cal.get(Calendar.DAY_OF_MONTH)
            map[d] = (map[d] ?: 0.0) + tx.amount
        }
    }
    return map
}
