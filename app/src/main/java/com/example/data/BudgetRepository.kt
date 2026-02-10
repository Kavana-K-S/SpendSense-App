package com.example.data

import android.content.Context

class BudgetRepository {

    suspend fun save(context: Context, budget: BudgetEntity) {
        AppDatabase.getDatabase(context).budgetDao().saveBudget(budget)
    }

    suspend fun get(context: Context, userId: String): BudgetEntity? {
        return AppDatabase.getDatabase(context).budgetDao().getBudget(userId)
    }

    suspend fun delete(context: Context, userId: String) {
        AppDatabase.getDatabase(context).budgetDao().deleteBudget(userId)
    }
}
