package com.example.tes.ui.reminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.entity.Obat
import com.example.tes.data.export.ExcelExporter
import com.example.tes.data.export.ExportObat
import com.example.tes.data.export.PdfExporter
import com.example.tes.data.repository.ObatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val obatRepository: ObatRepository
) : ViewModel() {

    fun getStokMenipis(): Flow<List<Obat>> = obatRepository.getStokMenipis()

    fun getAkanExpired(): Flow<List<Obat>> = obatRepository.getAkanExpired()

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    fun exportStokKritis(context: Context, format: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val hampirHabis = obatRepository.getStokMenipis().first()
                val akanExpired = obatRepository.getAkanExpired().first()
                val kategoris = obatRepository.getKategoris()

                fun Obat.toExportObat(): ExportObat {
                    val kategori = kategoris.find { it.id == this.kategoriId }
                    return ExportObat(
                        kode = this.kode,
                        nama = this.nama,
                        kategori = kategori?.nama ?: "-",
                        stok = this.stok,
                        stokMinimum = this.stokMinimum,
                        hargaBeli = this.hargaBeli,
                        hargaJual = this.hargaJual,
                        expiredDate = this.expiredDate
                    )
                }

                val exportHampirHabis = hampirHabis.map { it.toExportObat() }
                val exportAkanExpired = akanExpired.map { it.toExportObat() }

                val file = if (format == "PDF") {
                    PdfExporter(context).exportStokKritis(exportHampirHabis, exportAkanExpired)
                } else {
                    ExcelExporter(context).exportStokKritis(exportHampirHabis, exportAkanExpired)
                }
                _exportMessage.emit("Laporan tersimpan: ${file.name}")
            } catch (e: Exception) {
                _exportMessage.emit("Gagal: ${e.message}")
            }
        }
    }
}
