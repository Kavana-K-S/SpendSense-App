package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val userId: String,
    val amount: Double,
    val dateMillis: Long,
    val category: String,
    val type: String,

    // ✅ NEW FIELDS
    val paymentMethod: String = "Cash",
    val note: String = ""
)
