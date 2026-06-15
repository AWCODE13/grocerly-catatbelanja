package com.example.grocerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.grocerly.data.entity.ItemBelanjaEntity
import kotlinx.coroutines.flow.Flow
import com.example.grocerly.data.model.ListSummary

@Dao
interface ItemBelanjaDao {

    @Insert
    suspend fun insert(item: ItemBelanjaEntity)

    @Query("""
    SELECT * FROM item_belanja
    WHERE listId = :listId
    AND ownerEmail = :owner
    AND quantity > 0
    ORDER BY id DESC
""")
    fun getItemQtyGtZeroFlow(
        listId: Int,
        owner: String
    ): kotlinx.coroutines.flow.Flow<List<ItemBelanjaEntity>>


    @Query("SELECT * FROM item_belanja WHERE listId = :listId AND LOWER(namaBarang) = LOWER(:name) LIMIT 1")
    suspend fun getByName(listId: Int, name: String): ItemBelanjaEntity?

    @Query("UPDATE item_belanja SET quantity = :qty WHERE id = :id")
    suspend fun updateQtyById(id: Int, qty: Int)

    @Query("UPDATE item_belanja SET isChecked = :checked WHERE id = :id")
    suspend fun setChecked(id: Int, checked: Boolean)

    @Query("""
        UPDATE item_belanja
        SET namaBarang = :name, price = :price, quantity = :qty, unit = :unit
        WHERE id = :id
    """)
    suspend fun updateItem(id: Int, name: String, price: Long, qty: Int, unit: String)

    @Query("DELETE FROM item_belanja WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("""
    SELECT * FROM item_belanja
    WHERE listId = :listId
    AND ownerEmail = :owner
    ORDER BY id DESC
""")
    fun getItemByListFlow(listId: Int, owner: String): Flow<List<ItemBelanjaEntity>>

    @Query("""
    SELECT
        l.id AS id,
        l.judul AS judul,
        l.pinned AS pinned,
        l.budgetLimit AS budgetLimit,
        COALESCE(SUM(CASE WHEN i.isChecked = 1 THEN (i.price * i.quantity) ELSE 0 END), 0) AS spent,
        COALESCE(SUM(CASE WHEN i.quantity > 0 THEN 1 ELSE 0 END), 0) AS qty
    FROM list_belanja l
    LEFT JOIN item_belanja i ON i.listId = l.id
    WHERE l.isDeleted = 0
      AND l.ownerEmail = :owner
    GROUP BY l.id, l.judul, l.pinned, l.budgetLimit
    ORDER BY l.pinned DESC, l.id DESC
""")
    fun getHomeSummariesFlow(owner: String): kotlinx.coroutines.flow.Flow<List<ListSummary>>

    @Query("DELETE FROM item_belanja WHERE listId = :listId AND isChecked = 1")
    suspend fun deleteCheckedItems(listId: Int)

    @Query("SELECT * FROM item_belanja WHERE listId = :listId AND isChecked = 1")
    suspend fun getCheckedItemsOnce(listId: Int): List<ItemBelanjaEntity>

    @Query("DELETE FROM item_belanja WHERE listId = :listId")
    suspend fun deleteByListId(listId: Int)

    @Query("UPDATE item_belanja SET ownerEmail = :newEmail WHERE ownerEmail = :oldEmail")
    suspend fun updateOwnerEmail(oldEmail: String, newEmail: String)






}
