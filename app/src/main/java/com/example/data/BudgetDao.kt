package com.example.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budget WHERE userId = :userId LIMIT 1")
    suspend fun getBudget(userId: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBudget(budget: BudgetEntity)

    @Query("DELETE FROM budget WHERE userId = :userId")
    suspend fun deleteBudget(userId: String)
}
