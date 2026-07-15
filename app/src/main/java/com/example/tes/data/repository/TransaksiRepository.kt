package com.example.tes.data.repository

import com.example.tes.data.dao.ObatDao
import com.example.tes.data.dao.RiwayatStokDao
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.entity.TrenHari
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TransaksiRepository(
    private val obatDao: ObatDao,
    private val riwayatStokDao: RiwayatStokDao
) {
    fun getRiwayatByObat(obatId: Int): Flow<List<RiwayatStok>> =
        riwayatStokDao.getByObatId(obatId)

    fun getRiwayatByObatLimit(obatId: Int, limit: Int = 10): Flow<List<RiwayatStok>> =
        riwayatStokDao.getByObatIdLimit(obatId, limit)

    fun getRecentRiwayat(limit: Int = 50): Flow<List<RiwayatStok>> =
        riwayatStokDao.getRecent(limit)

    fun getTren7Hari(): Flow<List<TrenHari>> {
        val kalender = Calendar.getInstance()
        kalender.add(Calendar.DAY_OF_YEAR, -6)
        kalender.set(Calendar.HOUR_OF_DAY, 0)
        kalender.set(Calendar.MINUTE, 0)
        kalender.set(Calendar.SECOND, 0)
        kalender.set(Calendar.MILLISECOND, 0)
        return riwayatStokDao.getTren7Hari(kalender.timeInMillis)
    }

    fun getRiwayatByDateRange(startDate: Long, endDate: Long): Flow<List<RiwayatStok>> =
        riwayatStokDao.getByDateRange(startDate, endDate)

    suspend fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?) {
        val obat = obatDao.getById(obatId) ?: return
        val stokBaru = obat.stok + qty
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "MASUK",
                qty = qty,
                harga = harga,
                catatan = catatan
            )
        )
    }

    suspend fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?): Boolean {
        val obat = obatDao.getById(obatId) ?: return false
        if (obat.stok < qty) return false
        val stokBaru = obat.stok - qty
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "KELUAR",
                qty = qty,
                harga = harga,
                catatan = catatan
            )
        )
        return true
    }

    suspend fun penyesuaianStok(obatId: Int, stokBaru: Int, catatan: String?) {
        obatDao.getById(obatId) ?: return
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "PENYESUAIAN",
                qty = stokBaru,
                catatan = catatan
            )
        )
    }
}
