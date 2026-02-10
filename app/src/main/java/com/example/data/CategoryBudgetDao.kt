package com.example.data

import androidx.room.*

@Dao
interface CategoryBudgetDao {

    @Query("SELECT * FROM category_budgets WHERE userId = :userId")
    suspend fun getAll(userId: String): List<CategoryBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CategoryBudgetEntity)

    @Delete
    suspend fun delete(item: CategoryBudgetEntity)

    // ✅ ADD THIS (THIS FIXES YOUR ERROR)
    @Query("DELETE FROM category_budgets WHERE userId = :userId")
    suspend fun clearAll(userId: String)
}
