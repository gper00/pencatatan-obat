package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tes.data.entity.KategoriStok
import com.example.tes.data.entity.Obat
import kotlinx.coroutines.flow.Flow

@Dao
interface ObatDao {
    @Query("SELECT * FROM obat ORDER BY nama ASC")
    fun getAll(): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE id = :id")
    suspend fun getById(id: Int): Obat?

    @Query("SELECT * FROM obat WHERE nama LIKE '%' || :query || '%' OR kode LIKE '%' || :query || '%' ORDER BY nama ASC")
    fun search(query: String): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE stok <= stok_minimum ORDER BY stok ASC")
    fun getStokMenipis(): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE expired_date IS NOT NULL AND expired_date <= :tgl ORDER BY expired_date ASC")
    fun getAkanExpired(tgl: String): Flow<List<Obat>>

    @Query("SELECT COUNT(*) FROM obat")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM obat WHERE stok <= stok_minimum")
    fun countStokMenipis(): Flow<Int>

    @Query("SELECT COUNT(*) FROM obat WHERE expired_date IS NOT NULL AND expired_date <= :tgl")
    fun countAkanExpired(tgl: String): Flow<Int>

    @Query("""
        SELECT k.nama AS namaKategori, COALESCE(SUM(o.stok), 0) AS totalStok
        FROM kategori k
        LEFT JOIN obat o ON k.id = o.kategori_id
        GROUP BY k.id, k.nama
        ORDER BY totalStok DESC
    """)
    fun getStokPerKategori(): Flow<List<KategoriStok>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(obat: Obat): Long

    @Update
    suspend fun update(obat: Obat)

    @Delete
    suspend fun delete(obat: Obat)

    @Query("UPDATE obat SET stok = :stok WHERE id = :id")
    suspend fun updateStok(id: Int, stok: Int)
}
