package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tes.data.entity.Satuan
import kotlinx.coroutines.flow.Flow

@Dao
interface SatuanDao {
    @Query("SELECT * FROM satuan ORDER BY nama ASC")
    fun getAll(): Flow<List<Satuan>>

    @Query("SELECT * FROM satuan ORDER BY nama ASC")
    suspend fun getAllOnce(): List<Satuan>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(satuan: Satuan): Long

    @Delete
    suspend fun delete(satuan: Satuan)

    @Query("SELECT COUNT(*) FROM satuan")
    suspend fun count(): Int
}
