package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_budgets")
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: String,
    val category: String,
    val limitAmount: Double,
    val period: String = "Monthly",

    // ✅ ADD THESE
    val month: String,   // e.g. "January"
    val year: Int

)

