package com.example.grocerly.data.model

data class FinishedCartSummary(
    val id: Int,
    val judul: String,
    val budgetLimit: Long,
    val spent: Double,
    val qty: Int,
    val itemCount: Int
)
