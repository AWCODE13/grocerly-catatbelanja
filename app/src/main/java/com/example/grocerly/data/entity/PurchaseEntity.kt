package com.example.grocerly.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val listId: Int,
    val itemName: String,
    val qty: Int,
    val price: Double,
    val budgetLimit: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val ownerEmail: String = "guest"
)
