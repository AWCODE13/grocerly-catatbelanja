package com.example.grocerly.data.model

import com.example.grocerly.data.entity.BarangEntity

data class SelectableBarang(
    val barang: BarangEntity,
    var selected: Boolean = false,
    var qty: Int = 1
)