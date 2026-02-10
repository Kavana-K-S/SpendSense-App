package com.example.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import kotlinx.coroutines.Dispatchers
import com.example.data.AppDatabase
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import com.example.data.TransactionRepository



class SpendViewModel : ViewModel() {
    private val categoryBudgetRepository = CategoryBudgetRepository()

    private val repo = TransactionRepository()

    // ✅ SUMMARY STATE
    private val _totalIncomeState = mutableStateOf(0.0)
    val totalIncomeState: State<Double> = _totalIncomeState

    private val _totalExpenseState = mutableStateOf(0.0)
    val totalExpenseState: State<Double> = _totalExpenseState

    private val _categoryTotals = mutableStateOf<Map<String, Double>>(emptyMap())
    val categoryTotalsState: State<Map<String, Double>> = _categoryTotals

    private val _transactions = mutableStateOf<List<TransactionEntity>>(emptyList())
    val transactions: State<List<TransactionEntity>> = _transactions



    private fun format(value: Double): String {
        return NumberFormat.getNumberInstance(Locale("en", "IN")).format(value)
    }

    // ✅ LOAD SUMMARY (CALLED FROM DASHBOARD)
    fun loadSummary(context: Context) {
        viewModelScope.launch {

            val list = repo.getTransactions(context)
            _transactions.value = list   // 🔥 THIS LINE IS CRITICAL

            _totalIncomeState.value =
                list.filter { it.type.equals("income", true) }
                    .sumOf { it.amount }

            _totalExpenseState.value =
                list.filter { it.type.equals("expense", true) }
                    .sumOf { it.amount }

            _categoryTotals.value =
                list.filter { it.type.equals("expense", true) }
                    .groupBy { it.category ?: "Other" }
                    .mapValues { it.value.sumOf { tx -> tx.amount } }
        }
    }


    fun loadBudget(context: Context) {
        // unchanged
    }



    // ---------------------------------------------------------
    // PROFILE IMAGE
    // ---------------------------------------------------------
    val profileImage = mutableStateOf<String?>(null)

    fun loadProfileImage(context: Context) {
        FirebaseHelper.loadProfileImage(context) {
            profileImage.value = it
        }
    }
    // 🔹 REQUIRED FOR ANALYTICS SCREEN (ADAPTERS)

    fun totalIncome(): Double = totalIncomeState.value
    fun totalExpense(): Double = totalExpenseState.value


    fun categoryTotals(): Map<String, Double> {
        return transactions.value
            .filter { it.type.equals("expense", true) }
            .groupBy { it.category ?: "Other" }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
    }


    fun getHighestCategorySentence(): String {
        val entry = categoryTotals().maxByOrNull { it.value }
            ?: return "No expense data available"

        return "${entry.key} has the highest spending of ₹${format(entry.value)}"
    }
    fun getLowestCategorySentence(): String {
        val entry = categoryTotals().minByOrNull { it.value }
            ?: return "No expense data available"

        return "${entry.key} has the lowest spending of ₹${format(entry.value)}"
    }
    fun getSavingRateSentence(): String {
        val income = totalIncomeState.value
        val expense = totalExpenseState.value

        if (income <= 0) return "No income data to calculate savings"

        val savings = income - expense
        val rate = (savings / income) * 100

        return "You saved ${"%.1f".format(rate)}% of your income"
    }


    /* ---------------- BUDGET ---------------- */

    val budget = mutableStateOf(0.0)

    fun saveMainBudget(context: Context, amount: Double) {
        budget.value = amount
    }

    /* ---------------- CATEGORY BUDGETS ---------------- */

    private val _categoryBudgets = mutableStateListOf<CategoryBudgetEntity>()
    val categoryBudgets: List<CategoryBudgetEntity> get() = _categoryBudgets


    fun addOrUpdateCategoryBudget(context: Context, item: CategoryBudgetEntity) {
        viewModelScope.launch {
            categoryBudgetRepository.save(context, item)

            // 🔥 THIS LINE WAS MISSING
            loadCategoryBudgets(context)
        }
    }


    fun deleteCategoryBudget(context: Context, item: CategoryBudgetEntity) {
        viewModelScope.launch {
            categoryBudgetRepository.delete(context, item)

            // 🔥 THIS LINE WAS MISSING
            loadCategoryBudgets(context)
        }
    }

    fun renameCategoryInTransactions(context: Context, oldName: String, newName: String) {
        _transactions.value = _transactions.value.map { tx ->
            if (tx.category?.equals(oldName, ignoreCase = true) == true)
                tx.copy(category = newName)
            else tx
        }
    }

    fun loadCategoryBudgets(context: Context) {
        val userId = SessionManager(context).getUserId() ?: "local"

        viewModelScope.launch {
            val data = categoryBudgetRepository.getAll(context, userId)
            _categoryBudgets.clear()
            _categoryBudgets.addAll(data)
        }
    }

    fun exportCsv(
        context: Context,
        list: List<TransactionEntity>
    ): Uri? {
        if (list.isEmpty()) return null

        val file = File(context.cacheDir, "transactions.csv")
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        FileWriter(file).use { writer ->
            writer.append("Date,Category,Type,Amount\n")
            list.forEach {
                val dateStr = sdf.format(Date(it.dateMillis))   // ✅ FIX HERE
                writer.append("$dateStr,${it.category},${it.type},${it.amount}\n")
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }


    fun exportPdf(context: Context, list: List<TransactionEntity>): Uri? {
        if (list.isEmpty()) return null

        val file = File(context.cacheDir, "transactions.pdf")

        val document = android.graphics.pdf.PdfDocument()
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)

        val canvas = page.canvas
        val paint = android.graphics.Paint()
        paint.textSize = 12f

        var y = 40

        canvas.drawText("Date | Category | Type | Amount", 40f, y.toFloat(), paint)
        y += 25

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        list.forEach {
            if (y > 800) return@forEach  // prevent overflow

            val date = sdf.format(Date(it.dateMillis))
            canvas.drawText(
                "$date | ${it.category} | ${it.type} | ₹${it.amount}",
                40f,
                y.toFloat(),
                paint
            )
            y += 20
        }

        document.finishPage(page)
        document.writeTo(file.outputStream())
        document.close()

        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }





}

