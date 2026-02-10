package com.example.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.launch

class AddViewModel : ViewModel() {

    private val repo = TransactionRepository()

    val transactions: SnapshotStateList<TransactionEntity> =
        mutableStateListOf()

    fun loadTransactions(context: Context) {
        viewModelScope.launch {
            val list = repo.getTransactions(context)
            transactions.clear()
            transactions.addAll(list)
        }
    }

    fun addTransaction(
        context: Context,
        tx: TransactionEntity,
        spendVM: SpendViewModel
    ) {
        viewModelScope.launch {
            repo.insert(tx, context)

            // 🔥 REFRESH SUMMARY IMMEDIATELY
            spendVM.loadSummary(context)

            loadTransactions(context)
        }
    }


    fun deleteTransaction(
        context: Context,
        tx: TransactionEntity,
        spendVM: SpendViewModel
    ) {
        viewModelScope.launch {
            AppDatabase.getDatabase(context)
                .transactionDao()
                .delete(tx)

            loadTransactions(context)
            spendVM.loadSummary(context)   // 🔥 refresh summary
        }
    }

    fun updateTransaction(
        context: Context,
        tx: TransactionEntity,
        spendVM: SpendViewModel
    ) {
        viewModelScope.launch {
            AppDatabase.getDatabase(context)
                .transactionDao()
                .update(tx)

            loadTransactions(context)
            spendVM.loadSummary(context)   // 🔥 refresh summary
        }
    }


}
