package com.example.tes.ui.reminder

import androidx.lifecycle.ViewModel
import com.example.tes.data.entity.Obat
import com.example.tes.data.repository.ObatRepository
import kotlinx.coroutines.flow.Flow

class ReminderViewModel(
    private val obatRepository: ObatRepository
) : ViewModel() {

    fun getStokMenipis(): Flow<List<Obat>> = obatRepository.getStokMenipis()

    fun getAkanExpired(): Flow<List<Obat>> = obatRepository.getAkanExpired()
}
