package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val countryCode: String,
    val phone: String,
    val email: String,
    val occupation: String,
    val income: String,
    val currency: String,
    val imageBase64: String?
)
