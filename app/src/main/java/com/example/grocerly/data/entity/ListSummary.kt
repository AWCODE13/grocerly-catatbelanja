package com.example.grocerly.data.model

data class ListSummary(
    val id: Int,
    val judul: String,
    val pinned: Boolean,
    val budgetLimit: Long,
    val spent: Double,
    val qty: Int
)
