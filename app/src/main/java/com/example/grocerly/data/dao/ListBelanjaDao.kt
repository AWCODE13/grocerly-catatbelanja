package com.example.grocerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.grocerly.data.entity.ListBelanjaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListBelanjaDao {

    @Insert
    suspend fun insert(list: ListBelanjaEntity): Long

    @Query("SELECT * FROM list_belanja WHERE isDeleted = 0 ORDER BY pinned DESC, id DESC")
    fun getAllActiveFlow(): Flow<List<ListBelanjaEntity>>

    @Query("SELECT * FROM list_belanja WHERE isDeleted = 1 ORDER BY id DESC")
    suspend fun getAllDeleted(): List<ListBelanjaEntity>

    @Query("UPDATE list_belanja SET pinned = 1 WHERE id IN (:ids)")
    suspend fun pin(ids: List<Int>)

    @Query("UPDATE list_belanja SET pinned = 0 WHERE id IN (:ids)")
    suspend fun unpin(ids: List<Int>)

    @Query("UPDATE list_belanja SET isDeleted = 1, pinned = 0 WHERE id IN (:ids)")
    suspend fun softDelete(ids: List<Int>)

    @Query("UPDATE list_belanja SET isDeleted = 0 WHERE id IN (:ids)")
    suspend fun restore(ids: List<Int>)

    @Query("DELETE FROM list_belanja WHERE id IN (:ids)")
    suspend fun deletePermanent(ids: List<Int>)

    @Query("UPDATE list_belanja SET pinned = CASE WHEN pinned = 1 THEN 0 ELSE 1 END WHERE id IN (:ids)")
    suspend fun togglePin(ids: List<Int>)

    @Query("UPDATE list_belanja SET budgetLimit = :budget WHERE id = :listId")
    suspend fun updateBudget(listId: Int, budget: Long)

    @Query("SELECT * FROM list_belanja WHERE id = :listId LIMIT 1")
    suspend fun getById(listId: Int): ListBelanjaEntity?

    @Query("UPDATE list_belanja SET judul = :newTitle WHERE id = :listId")
    suspend fun updateTitle(listId: Int, newTitle: String)

    @Query("DELETE FROM list_belanja WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
    SELECT * FROM list_belanja
    WHERE id = :listId
    AND ownerEmail = :owner
    LIMIT 1
""")
    suspend fun getByIdAndOwner(
        listId: Int,
        owner: String
    ): ListBelanjaEntity?

    @Query("UPDATE list_belanja SET ownerEmail = :newEmail WHERE ownerEmail = :oldEmail")
    suspend fun updateOwnerEmail(oldEmail: String, newEmail: String)

}
