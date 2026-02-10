package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()

    @Update
    suspend fun update(tx: TransactionEntity)
    @Delete
    suspend fun delete(tx: TransactionEntity)



    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getByUser(userId: String): List<TransactionEntity>
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC")
    suspend fun getAll(userId: String): List<TransactionEntity>
}


