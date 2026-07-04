package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tes.data.entity.Kategori
import kotlinx.coroutines.flow.Flow

@Dao
interface KategoriDao {
    @Query("SELECT * FROM kategori ORDER BY nama ASC")
    fun getAll(): Flow<List<Kategori>>

    @Query("SELECT * FROM kategori ORDER BY nama ASC")
    suspend fun getAllOnce(): List<Kategori>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(kategori: Kategori): Long

    @Delete
    suspend fun delete(kategori: Kategori)

    @Query("SELECT COUNT(*) FROM kategori")
    suspend fun count(): Int
}
