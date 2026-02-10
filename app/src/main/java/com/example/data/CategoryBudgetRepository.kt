package com.example.data

import android.content.Context

class CategoryBudgetRepository {

    suspend fun getAll(context: Context, userId: String): List<CategoryBudgetEntity> {
        return AppDatabase.getDatabase(context)
            .categoryBudgetDao()
            .getAll(userId)
    }

    suspend fun save(context: Context, item: CategoryBudgetEntity) {
        AppDatabase.getDatabase(context)
            .categoryBudgetDao()
            .insert(item)
    }

    suspend fun delete(context: Context, item: CategoryBudgetEntity) {
        AppDatabase.getDatabase(context)
            .categoryBudgetDao()
            .delete(item)
    }

    suspend fun clearAll(context: Context, userId: String) {
        AppDatabase.getDatabase(context)
            .categoryBudgetDao()
            .clearAll(userId)
    }
}
