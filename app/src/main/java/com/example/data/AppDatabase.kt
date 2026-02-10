package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        TransactionEntity::class,
        CategoryBudgetEntity::class,
        BudgetEntity::class,
        NotificationEntity::class,
        ProfileEntity::class
],
    version = 4,   // incremented version
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryBudgetDao(): CategoryBudgetDao
    abstract fun notificationDao(): NotificationDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendsense_db"
                )
                    .fallbackToDestructiveMigration() // 🔥 REQUIRED
                    .build()

                INSTANCE = instance
                instance
            }
        }

    }
}
