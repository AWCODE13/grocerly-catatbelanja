package com.example.grocerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.grocerly.data.entity.PurchaseEntity
import com.example.grocerly.data.model.ListSummary
import kotlinx.coroutines.flow.Flow
import com.example.grocerly.data.model.FinishedCartSummary

@Dao
interface PurchaseDao {

    @Insert
    suspend fun insertAll(items: List<PurchaseEntity>)

    @Query("""
        SELECT l.id, l.judul, l.pinned, l.budgetLimit,
               COALESCE(SUM(p.price * p.qty), 0) AS spent,
               COALESCE(SUM(p.qty), 0) AS qty
        FROM list_belanja l
        LEFT JOIN purchase p ON p.listId = l.id
        WHERE l.isDeleted = 0
        GROUP BY l.id
        ORDER BY l.pinned DESC, l.id DESC
    """)
    fun getHomeSummariesFlow(): Flow<List<ListSummary>>

    @Query("""
        SELECT COALESCE(SUM(price * qty), 0)
        FROM purchase
        WHERE listId = :listId
    """)
    fun totalSpentByListFlow(listId: Int): Flow<Double>

    @Query("""
        SELECT COALESCE(SUM(qty), 0)
        FROM purchase
        WHERE listId = :listId
    """)
    fun totalQtyByListFlow(listId: Int): Flow<Int>

    @Query("""SELECT COALESCE(SUM(price * qty), 0) FROM purchase""")
    fun totalSpentAllFlow(): Flow<Double>

    @Query("""SELECT COALESCE(SUM(qty), 0) FROM purchase""")
    fun totalQtyAllFlow(): Flow<Int>

    @Query("""
        SELECT COALESCE(SUM(price * qty), 0)
        FROM purchase
        WHERE createdAt >= :from
    """)
    suspend fun totalSpending(from: Long): Double

    @Query("""
        SELECT COALESCE(SUM(qty), 0)
        FROM purchase
        WHERE createdAt >= :from
    """)
    suspend fun totalQty(from: Long): Int

    @Query("""
    SELECT * FROM purchase
    ORDER BY createdAt DESC
    LIMIT 5
""")
    suspend fun getRecentPurchases(): List<PurchaseEntity>

    @Insert
    suspend fun insert(p: PurchaseEntity)

    @Query("""
    SELECT
        p.listId AS id,
        'Finished Cart' AS judul,
        MAX(p.budgetLimit) AS budgetLimit,
        COALESCE(SUM(p.price * p.qty), 0) AS spent,
        COALESCE(SUM(p.qty), 0) AS qty,
        COUNT(p.id) AS itemCount
    FROM purchase p
    WHERE p.ownerEmail = :ownerEmail
    GROUP BY p.listId
    ORDER BY MAX(p.createdAt) DESC
    LIMIT 20
""")
    fun getRecentFinishedCartsFlow(
        ownerEmail: String
    ): kotlinx.coroutines.flow.Flow<List<com.example.grocerly.data.model.FinishedCartSummary>>

    @Query("""
    SELECT
        (createdAt / 86400000) * 86400000 AS dayStart,
        COALESCE(SUM(price * qty), 0) AS total
    FROM purchase
    WHERE createdAt >= :fromMillis
      AND ownerEmail = :ownerEmail
    GROUP BY dayStart
    ORDER BY dayStart ASC
""")
    fun getDailySpendingFlow(
        fromMillis: Long,
        ownerEmail: String
    ): Flow<List<com.example.grocerly.data.model.DailySpending>>

    @Query("UPDATE purchase SET ownerEmail = :newEmail WHERE ownerEmail = :oldEmail")
    suspend fun updateOwnerEmail(oldEmail: String, newEmail: String)

}
