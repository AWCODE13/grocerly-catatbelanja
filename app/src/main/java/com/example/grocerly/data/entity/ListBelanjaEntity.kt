package com.example.grocerly.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "list_belanja")
data class ListBelanjaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val judul: String,
    val pinned: Boolean = false,
    val isDeleted: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    val budgetLimit: Long = 0L,

    @ColumnInfo(defaultValue = "guest")
    val ownerEmail: String = "guest"
)
