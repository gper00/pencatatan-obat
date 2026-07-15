package com.example.tes.data.repository

import com.example.tes.data.dao.KategoriDao
import com.example.tes.data.dao.ObatDao
import com.example.tes.data.dao.SatuanDao
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.KategoriStok
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.Satuan
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ObatRepository(
    private val obatDao: ObatDao,
    private val kategoriDao: KategoriDao,
    private val satuanDao: SatuanDao
) {
    fun getAllObat(): Flow<List<Obat>> = obatDao.getAll()

    fun searchObat(query: String): Flow<List<Obat>> = obatDao.search(query)

    suspend fun getObatById(id: Int): Obat? = obatDao.getById(id)

    suspend fun insertObat(obat: Obat): Long = obatDao.insert(obat)

    suspend fun updateObat(obat: Obat) = obatDao.update(obat)

    suspend fun deleteObat(obat: Obat) = obatDao.delete(obat)

    suspend fun getKategoris(): List<Kategori> = kategoriDao.getAllOnce()

    fun getAllKategoris(): Flow<List<Kategori>> = kategoriDao.getAll()

    suspend fun getSatuans(): List<Satuan> = satuanDao.getAllOnce()

    fun getAllSatuans(): Flow<List<Satuan>> = satuanDao.getAll()

    fun getStokMenipis(): Flow<List<Obat>> = obatDao.getStokMenipis()

    fun getAkanExpired(): Flow<List<Obat>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        val tgl = dateFormat.format(cal.time)
        return obatDao.getAkanExpired(tgl)
    }

    fun getStokPerKategori(): Flow<List<KategoriStok>> = obatDao.getStokPerKategori()

    fun getAllObatOnce(): Flow<List<Obat>> = obatDao.getAll()

    fun getAllKategorisOnce(): Flow<List<Kategori>> = kategoriDao.getAll()

    fun getAllSatuansOnce(): Flow<List<Satuan>> = satuanDao.getAll()

    fun countAll(): Flow<Int> = obatDao.countAll()
    fun countStokMenipis(): Flow<Int> = obatDao.countStokMenipis()
    fun countAkanExpired(): Flow<Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        val tgl = dateFormat.format(cal.time)
        return obatDao.countAkanExpired(tgl)
    }
}
