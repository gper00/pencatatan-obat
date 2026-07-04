package com.example.tes.ui.transaksi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TransaksiViewModel(
    private val obatRepository: ObatRepository,
    private val transaksiRepository: TransaksiRepository
) : ViewModel() {

    fun searchObat(query: String): Flow<List<Obat>> = obatRepository.searchObat(query)

    val allObat: StateFlow<List<Obat>> = obatRepository.getAllObat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?) {
        transaksiRepository.stokMasuk(obatId, qty, harga, catatan)
    }

    suspend fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?): Boolean {
        return transaksiRepository.stokKeluar(obatId, qty, harga, catatan)
    }

    fun getRiwayatByObat(obatId: Int): Flow<List<RiwayatStok>> =
        transaksiRepository.getRiwayatByObat(obatId)

    fun getRecentRiwayat(): Flow<List<RiwayatStok>> =
        transaksiRepository.getRecentRiwayat(50)
}
