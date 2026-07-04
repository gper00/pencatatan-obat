package com.example.tes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.DataSeeder
import com.example.tes.data.repository.ObatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val obatRepository: ObatRepository,
    private val dataSeeder: DataSeeder
) : ViewModel() {

    val totalObat: StateFlow<Int> = obatRepository.countAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stokMenipis: StateFlow<Int> = obatRepository.countStokMenipis()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val akanExpired: StateFlow<Int> = obatRepository.countAkanExpired()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _isSeeding = MutableStateFlow(false)
    val isSeeding: StateFlow<Boolean> = _isSeeding.asStateFlow()

    private val _seedMessage = MutableStateFlow<String?>(null)
    val seedMessage: StateFlow<String?> = _seedMessage.asStateFlow()

    fun seedSampleData() {
        if (_isSeeding.value) return
        viewModelScope.launch {
            _isSeeding.value = true
            _seedMessage.value = null
            try {
                dataSeeder.seed()
                _seedMessage.value = "Data contoh berhasil ditambahkan!"
            } catch (e: Exception) {
                _seedMessage.value = "Gagal menambah data: ${e.message}"
            } finally {
                _isSeeding.value = false
            }
        }
    }

    fun clearSeedMessage() {
        _seedMessage.value = null
    }
}
