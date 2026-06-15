package com.example.grocerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.grocerly.data.entity.BarangEntity

@Dao
interface BarangDao {

    @Insert
    suspend fun insertAll(barang: List<BarangEntity>)

    @Query("SELECT * FROM barang LIMIT 10")
    suspend fun getRekomendasi(): List<BarangEntity>

    @Query("SELECT * FROM barang WHERE nama LIKE '%' || :keyword || '%'")
    suspend fun searchBarang(keyword: String): List<BarangEntity>

    @Query("SELECT COUNT(*) FROM barang")
    suspend fun countBarang(): Int

    @Query("SELECT * FROM barang WHERE nama = :nama LIMIT 1")
    suspend fun getByNamaExact(nama: String): BarangEntity?

    @Query("SELECT COUNT(*) FROM barang WHERE LOWER(nama) = LOWER(:nama) LIMIT 1")
    suspend fun existsByNama(nama: String): Int


    @Insert
    suspend fun insert(barang: BarangEntity): Long



}
