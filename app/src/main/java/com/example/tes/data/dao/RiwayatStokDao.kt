package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tes.data.entity.RiwayatStok
import kotlinx.coroutines.flow.Flow

@Dao
interface RiwayatStokDao {
    @Query("SELECT * FROM riwayat_stok WHERE obat_id = :obatId ORDER BY created_at DESC")
    fun getByObatId(obatId: Int): Flow<List<RiwayatStok>>

    @Query("SELECT * FROM riwayat_stok ORDER BY created_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<RiwayatStok>>

    @Query("""
        SELECT * FROM riwayat_stok 
        WHERE obat_id = :obatId 
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    fun getByObatIdLimit(obatId: Int, limit: Int = 10): Flow<List<RiwayatStok>>

    @Insert
    suspend fun insert(riwayat: RiwayatStok): Long
}
