package com.example.spendscreen.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.viewmodel.SpendViewModel
import kotlin.math.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalContext

/* ---------- Analytics Screen ---------- */

@Composable
fun AnalyticsScreen(vm: SpendViewModel) {

    val ctx = LocalContext.current
    val income by vm.totalIncomeState
    val expense by vm.totalExpenseState



    var selectedTab by remember { mutableStateOf("Category") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // ⭐ ADD THIS
            .padding(12.dp)
    ){


    // Top summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SummaryCardSmall(
                title = "💰 Income",
                amount = income,
                modifier = Modifier.weight(1f)
            )

            SummaryCardSmall(
                title = "💸 Expense",
                amount = expense,
                modifier = Modifier.weight(1f)
            )

            SummaryCardSmall(
                title = "🪙 Remaining",
                amount = income - expense,
                modifier = Modifier.weight(1f)
            )

        }

        Spacer(Modifier.height(16.dp))

        // Tabs
        Row(modifier = Modifier.fillMaxWidth()) {
            ToggleTabButton("Category", selectedTab == "Category") { selectedTab = "Category" }
            Spacer(Modifier.width(8.dp))
            ToggleTabButton("Weekly Trend", selectedTab == "Weekly") { selectedTab = "Weekly" }
            Spacer(Modifier.width(8.dp))
            ToggleTabButton("Monthly Trend", selectedTab == "Monthly") { selectedTab = "Monthly" }
        }

        Spacer(Modifier.height(16.dp))

        when (selectedTab) {
            "Category" -> CategoryTab(vm)
            "Weekly" -> WeeklyTab(vm)
            "Monthly" -> MonthlyTab(vm)
        }

        Spacer(Modifier.height(16.dp))


    }
}

/* ---------- summary card ---------- */
@Composable
fun SummaryCardSmall(title: String, amount: Double, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(84.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FD)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF512DA8))
            Spacer(Modifier.height(6.dp))
            Text("₹ ${"%.2f".format(amount)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF311B92))
        }
    }
}

/* ---------- Toggle tab ---------- */
@Composable
fun ToggleTabButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1976D2) else Color(0xFFE3F2FD),
            contentColor = if (selected) Color.White else Color(0xFF1976D2)
        ),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text)
    }
}

/* ---------- Category tab ---------- */
@Composable
fun CategoryTab(vm: SpendViewModel) {
    Column {
        Text(
            text = "Category-wise Spending",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(12.dp))

        // Get total per category (amounts)
        val totals = vm.categoryTotals()

        if (totals.isEmpty()) {
            Text("No expense data yet.", modifier = Modifier.align(Alignment.CenterHorizontally))
            return
        }

        // Prepare pie-chart items: (category, amount, color)
        val items = totals.map { (cat, amount) ->
            Triple(cat, amount, categoryColor(cat))
        }.sortedByDescending { it.second }

        // Show Pie Chart
        PieChartAnimated(
            items = items,
            modifier = Modifier.fillMaxWidth(),
            showButton = false
        )

        Spacer(Modifier.height(12.dp))

        // Category rows with progress bars
        Column {
            val totalExpense = vm.totalExpense().coerceAtLeast(1.0)

            items.forEach { (cat, amt, color) ->
                val pct = (amt / totalExpense) * 100.0
                RichCategoryCard(cat = cat, pct = pct, amount = amt, color = color)
                Spacer(Modifier.height(8.dp))
            }
        }
        // Key insights (kept here in case you want it repeated bottom)

        if (totals.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Key Insights", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Text("🔺 ${vm.getHighestCategorySentence()}")
                    Spacer(Modifier.height(6.dp))
                    Text("🔻 ${vm.getLowestCategorySentence()}")
                    Spacer(Modifier.height(6.dp))
                    Text("💡 ${vm.getSavingRateSentence()}")
                }
            }
        }
    }
}


/* ---------- Rich category row ---------- */
@Composable
fun RichCategoryCard(cat: String, pct: Double, amount: Double, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.06f), shape = RoundedCornerShape(12.dp))
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = categoryEmoji(cat), fontSize = 20.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("$cat", fontWeight = FontWeight.SemiBold)
                    Text("${"%.0f".format(pct)}%", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                val fraction = (pct.coerceIn(0.0, 100.0) / 100.0).toFloat()
                LinearProgressIndicator(
                    progress = fraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    trackColor = Color.LightGray.copy(alpha = 0.2f),
                    color = color
                )

                Spacer(Modifier.height(6.dp))
                Text("Spent: ₹ ${"%.2f".format(amount)}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

/* ---------- Pie chart ---------- */
@Composable
fun PieChartAnimated(
    items: List<Triple<String, Double, Color>>,
    modifier: Modifier = Modifier,
    animationDurationMs: Int = 900,
    showButton: Boolean = true,
    explodeOffset: Float = 14f
) {
    val total = items.sumOf { it.second }.coerceAtLeast(1.0)

    val progress = remember { Animatable(0f) }
    LaunchedEffect(items) {
        progress.snapTo(0f)
        progress.animateTo(
            1f,
            tween(animationDurationMs, easing = FastOutSlowInEasing)
        )
    }

    // Selected index used for both offset & dialog content
    var selectedSliceIndex by remember { mutableStateOf(-1) }

    // Dialog control
    var showDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .pointerInput(items) {
                        detectTapGestures { tap ->
                            // Detect which slice was tapped
                            val radius = min(size.width, size.height) / 2f
                            val cx = size.width / 2f
                            val cy = size.height / 2f

                            val dx = tap.x - cx
                            val dy = tap.y - cy
                            val distance = sqrt(dx * dx + dy * dy)
                            if (distance > radius) {
                                // outside pie
                                return@detectTapGestures
                            }

                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            angle += 90f
                            if (angle < 0) angle += 360f

                            var startAngle = 0f
                            items.forEachIndexed { idx, item ->
                                val sweep = (item.second / total * 360f).toFloat()
                                if (angle >= startAngle && angle < startAngle + sweep) {
                                    selectedSliceIndex = if (selectedSliceIndex == idx) -1 else idx
                                    // open dialog when a slice is selected
                                    showDialog = selectedSliceIndex != -1
                                }
                                startAngle += sweep
                            }
                        }
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = min(size.width, size.height) / 2f

                var startAngle = -90f

                items.forEachIndexed { idx, (cat, pct, baseColor) ->
                    val sweepFull = (pct / total * 360f).toFloat()
                    val sweep = sweepFull * progress.value

                    val isSelected = idx == selectedSliceIndex
                    val offset = if (isSelected) explodeOffset else 0f
                    val alpha = if (selectedSliceIndex == -1 || isSelected) 1f else 0.6f

                    val midAngleRad = Math.toRadians((startAngle + sweep / 2).toDouble())
                    val shiftX = (offset * cos(midAngleRad)).toFloat()
                    val shiftY = (offset * sin(midAngleRad)).toFloat()

                    drawArc(
                        color = baseColor.copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(cx - r + shiftX, cy - r + shiftY),
                        size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                    )

                    startAngle += sweep
                }
            }
        }

        // Dialog: centered AlertDialog with requested content format
        if (showDialog && selectedSliceIndex in items.indices) {
            val (cat, amount, col) = items[selectedSliceIndex]

            val totalAmount = items.sumOf { it.second }
            val percent = if (totalAmount <= 0) 0.0 else (amount / totalAmount) * 100.0

            AlertDialog(
                onDismissRequest = { showDialog = false; selectedSliceIndex = -1 },
                confirmButton = {
                    TextButton(onClick = { showDialog = false; selectedSliceIndex = -1 }) { Text("OK") }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(categoryEmoji(cat), fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(cat, fontWeight = FontWeight.SemiBold)
                    }
                },
                text = {
                    Text("Spent ${"%.1f".format(percent)}% or ₹ ${"%.2f".format(amount)}")
                }
            )
        }

    }
}

/* ---------- Weekly tab ---------- */
@Composable
fun WeeklyTab(vm: SpendViewModel) {
    Card(

        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Weekly Trends (Last 7 Days)", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            val weekly = last7DaysTrendsFromEntities(vm.transactions.value)

            DualBarChartDaily(weekly)

            Spacer(Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF4CAF50), shape = RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("Income", fontSize = 12.sp)
                    Spacer(Modifier.width(14.dp))
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFF44336), shape = RoundedCornerShape(3.dp)))
                    Spacer(Modifier.width(6.dp))
                    Text("Expense", fontSize = 12.sp)
                }

                val dayNet = weekly.mapIndexed { _, p -> p.second.first - p.second.second }
                val maxIdx = dayNet.indices.maxByOrNull { dayNet[it] } ?: 0
                Text("Top net day: ${weekly[maxIdx].first}", fontSize = 12.sp)
            }
        }
    }
}

/* ---------- Dual bar chart ---------- */
@Composable
fun DualBarChartDaily(data: List<Pair<String, Pair<Double, Double>>>) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxValue = data.maxOfOrNull { max(it.second.first, it.second.second) } ?: 1.0

    Column(modifier = Modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)   // <-- Increased height so labels always fit
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {

            data.forEachIndexed { index, (label, pair) ->

                val incHeight = if (maxValue <= 0) 0.dp else ((pair.first / maxValue) * 130).dp
                val expHeight = if (maxValue <= 0) 0.dp else ((pair.second / maxValue) * 130).dp

                Column(
                    modifier = Modifier
                        .width(44.dp)
                        .clickable { selectedIndex = index }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    // INCOME bar (green)
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(incHeight)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50))
                    )

                    Spacer(Modifier.height(6.dp))

                    // EXPENSE bar (red)
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(expHeight)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF44336))
                    )

                    Spacer(Modifier.height(10.dp))   // <-- IMPORTANT: This guarantees label space

                    Text(
                        label,
                        fontSize = 13.sp,
                        modifier = Modifier.height(20.dp), // <-- Label always visible
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Dialog on bar tap
    selectedIndex?.let { idx ->
        val (label, pair) = data[idx]
        AlertDialog(
            onDismissRequest = { selectedIndex = null },
            title = { Text("Details — $label") },
            text = {
                Column {
                    Text("Income: ₹ ${"%.2f".format(pair.first)}")
                    Spacer(Modifier.height(6.dp))
                    Text("Expense: ₹ ${"%.2f".format(pair.second)}")
                    Spacer(Modifier.height(6.dp))
                    Text("Net: ₹ ${"%.2f".format(pair.first - pair.second)}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedIndex = null }) { Text("OK") }
            }
        )
    }
}


/* ---------- Monthly tab ---------- */
@Composable
fun MonthlyTab(vm: SpendViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().height(420.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Monthly Trends (Last 6 Months)", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            val monthly = last6MonthsTrendsFromEntities(vm.transactions.value)

            // Small legend
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF4CAF50)))
                    Spacer(Modifier.width(4.dp))
                    Text("Income", fontSize = 12.sp)
                    Spacer(Modifier.width(12.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFE57373)))
                    Spacer(Modifier.width(4.dp))
                    Text("Expense", fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            MonthlyLineChart(monthly)
        }
    }
}

/* ---------- Monthly line chart ---------- */
@Composable
fun MonthlyLineChart(data: List<Triple<String, Double, Double>>) {
    val maxVal = data.maxOfOrNull { max(it.second, it.third) } ?: 1.0

    Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 20.dp)) {
            val w = size.width
            val h = size.height
            val count = data.size.coerceAtLeast(1)
            val segment = w / count

            val incomePoints = data.mapIndexed { idx, (_, income, _) ->
                val x = segment * idx + segment / 2f
                val safeIncome = if (income == 0.0) 0.0001 else income
                val yRatio = if (maxVal <= 0.0) 0f else (safeIncome / maxVal).toFloat()
                val y = h - (yRatio * (h * 0.6f)) - 20f
                Offset(x, y)
            }

            val expensePoints = data.mapIndexed { idx, (_, _, expense) ->
                val x = segment * idx + segment / 2f
                val yRatio = if (maxVal <= 0.0) 0f else (expense / maxVal).toFloat()
                val y = h - (yRatio * (h * 0.6f)) - 20f
                Offset(x, y)
            }

            // baseline
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, h - 8f),
                end = Offset(w, h - 8f),
                strokeWidth = 1f
            )

            if (incomePoints.isNotEmpty()) {
                val path = Path().apply {
                    moveTo(incomePoints.first().x, incomePoints.first().y)
                    for (i in 1 until incomePoints.size) lineTo(incomePoints[i].x, incomePoints[i].y)
                }
                drawPath(
                    path,
                    color = Color(0xFF2E7D32),     // darker green
                    style = Stroke(width = 5f, cap = StrokeCap.Round)  // thicker
                )


                incomePoints.forEach { p -> drawCircle(color = Color(0xFF4CAF50), radius = 4f, center = p) }
            }

            if (expensePoints.isNotEmpty()) {
                val path2 = Path().apply {
                    moveTo(expensePoints.first().x, expensePoints.first().y)
                    for (i in 1 until expensePoints.size) lineTo(expensePoints[i].x, expensePoints[i].y)
                }
                drawPath(
                    path2,
                    color = Color(0xFFC62828),    // darker red
                    style = Stroke(width = 4f, cap = StrokeCap.Round)  // thicker
                )
                expensePoints.forEach { p -> drawCircle(color = Color(0xFFE57373).copy(alpha = 0.8f), radius = 3.5f, center = p) }
            }
        }

        // labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { (label, _, _) ->
                Text(label, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(48.dp))
            }
        }
    }
}

/* ---------- Helpers ---------- */

fun last7DaysTrendsFromEntities(entities: List<TransactionEntity>): List<Pair<String, Pair<Double, Double>>> {
    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val result = mutableListOf<Pair<String, Pair<Double, Double>>>()
    for (i in 6 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        val key = sdfKey.format(c.time)
        val inc = entities.filter { sdfKey.format(Date(it.dateMillis)) == key && it.type == "income" }.sumOf { it.amount }
        val exp = entities.filter { sdfKey.format(Date(it.dateMillis)) == key && it.type == "expense" }.sumOf { it.amount }
        val dayLabel = SimpleDateFormat("E", Locale.getDefault()).format(c.time) // Sun/Mon
        result.add(dayLabel to (inc to exp))
    }
    return result
}

fun last6MonthsTrendsFromEntities(entities: List<TransactionEntity>): List<Triple<String, Double, Double>> {
    val sdfKey = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    val monthLabel = SimpleDateFormat("MMM", Locale.getDefault())
    val months = mutableListOf<String>()
    for (i in 5 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.MONTH, -i)
        months.add(sdfKey.format(c.time))
    }
    val result = mutableListOf<Triple<String, Double, Double>>()
    months.forEach { key ->
        val inc = entities.filter { sdfKey.format(Date(it.dateMillis)) == key && it.type == "income" }.sumOf { it.amount }
        val exp = entities.filter { sdfKey.format(Date(it.dateMillis)) == key && it.type == "expense" }.sumOf { it.amount }
        val date = sdfKey.parse(key)
        val label = if (date != null) monthLabel.format(date) else key
        result.add(Triple(label, inc, exp))
    }
    return result
}

fun categoryEmoji(category: String): String {
    return when (category.lowercase(Locale.getDefault())) {
        "food", "food & dining", "food & dine" -> "🍔"
        "transport", "transportation" -> "🚗"
        "healthcare", "health" -> "🏥"
        "education" -> "🎓"
        "bills", "bills & utilities", "utilities" -> "💡"
        "rent", "housing" -> "🏠"
        "shopping", "shop" -> "🛍️"
        else -> "📦"
    }
}

fun categoryColor(category: String): Color =
    when (category) {
        "Rent" -> Color(0xFFE57373)
        "Bills & Utilities", "Bills" -> Color(0xFF64B5F6)
        "Food & Dining", "Food" -> Color(0xFF81C784)
        "Transportation", "Transport" -> Color(0xFFFFB74D)
        "Shopping", "Shopping" -> Color(0xFFF06292)
        "Education" -> Color(0xFFBA68C8)
        "Healthcare" -> Color(0xFFFF8A65)
        "Other" -> Color(0xFF90A4AE)
        else -> Color(0xFFB0BEC5)
    }
