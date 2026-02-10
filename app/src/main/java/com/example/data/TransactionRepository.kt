package com.example.data

import android.content.Context

class TransactionRepository {

    suspend fun getTransactions(context: Context): List<TransactionEntity> {
        val userId = SessionManager(context).getUserId() ?: return emptyList()
        val db = AppDatabase.getDatabase(context)
        return db.transactionDao().getAll(userId)
    }


    suspend fun insert(tx: TransactionEntity, context: Context) {
        // ✅ SAME DATABASE METHOD
        val db = AppDatabase.getDatabase(context)
        db.transactionDao().insert(tx)
    }

    suspend fun clearAll(context: Context) {
        // ✅ SAME DATABASE METHOD
        val db = AppDatabase.getDatabase(context)
        db.transactionDao().clearAll()
    }
}

