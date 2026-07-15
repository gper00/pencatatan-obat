package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.entity.TrenHari
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

    @Query("""
        SELECT DATE(created_at / 1000, 'unixepoch') AS tanggal,
               COALESCE(SUM(CASE WHEN jenis = 'MASUK' THEN qty ELSE 0 END), 0) AS masuk,
               COALESCE(SUM(CASE WHEN jenis = 'KELUAR' THEN qty ELSE 0 END), 0) AS keluar
        FROM riwayat_stok
        WHERE created_at >= :sejak
        GROUP BY DATE(created_at / 1000, 'unixepoch')
        ORDER BY tanggal ASC
    """)
    fun getTren7Hari(sejak: Long): Flow<List<TrenHari>>

    @Query("SELECT * FROM riwayat_stok WHERE created_at >= :startDate AND created_at <= :endDate ORDER BY created_at DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<RiwayatStok>>

    @Insert
    suspend fun insert(riwayat: RiwayatStok): Long
}
