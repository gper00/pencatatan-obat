package com.example.tes.ui.obat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.Satuan
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ObatViewModel(
    private val obatRepository: ObatRepository,
    private val transaksiRepository: TransaksiRepository
) : ViewModel() {

    val obatList: StateFlow<List<Obat>> = obatRepository.getAllObat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kategoriList: StateFlow<List<Kategori>> = obatRepository.getAllKategoris()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val satuanList: StateFlow<List<Satuan>> = obatRepository.getAllSatuans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun searchObat(query: String): StateFlow<List<Obat>> {
        return obatRepository.searchObat(query)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun insertObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.insertObat(obat)
            callback()
        }
    }

    fun updateObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.updateObat(obat)
            callback()
        }
    }

    fun deleteObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.deleteObat(obat)
            callback()
        }
    }

    private val _selectedObat = MutableStateFlow<Obat?>(null)
    val selectedObat: StateFlow<Obat?> = _selectedObat.asStateFlow()

    fun loadObat(id: Int) {
        viewModelScope.launch {
            _selectedObat.value = obatRepository.getObatById(id)
        }
    }

    fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?, callback: () -> Unit = {}) {
        viewModelScope.launch {
            transaksiRepository.stokMasuk(obatId, qty, harga, catatan)
            loadObat(obatId)
            callback()
        }
    }

    fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = transaksiRepository.stokKeluar(obatId, qty, harga, catatan)
            if (success) loadObat(obatId)
            callback(success)
        }
    }
}
