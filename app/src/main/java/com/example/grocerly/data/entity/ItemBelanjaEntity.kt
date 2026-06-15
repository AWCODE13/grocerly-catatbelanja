package com.example.grocerly.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_belanja")
data class ItemBelanjaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listId: Int,
    val namaBarang: String,
    val quantity: Int = 0,
    val price: Long = 0L,
    val unit: String = "",
    val isChecked: Boolean = false,
    val ownerEmail: String = "guest"
)
