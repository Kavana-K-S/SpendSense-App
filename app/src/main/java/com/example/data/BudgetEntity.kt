package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey val userId: String,
    val amount: Double,
    val period: String   // Monthly / Weekly
)

