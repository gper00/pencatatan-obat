package com.example.tes.ui.transaksi

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.export.ExcelExporter
import com.example.tes.data.export.ExportRiwayat
import com.example.tes.data.export.PdfExporter
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class RiwayatViewModel(
    private val transaksiRepository: TransaksiRepository,
    private val obatRepository: ObatRepository
) : ViewModel() {

    private val _startDate = MutableStateFlow(
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -30) }.timeInMillis
    )
    private val _endDate = MutableStateFlow(System.currentTimeMillis())

    val startDate: StateFlow<Long> = _startDate.asStateFlow()
    val endDate: StateFlow<Long> = _endDate.asStateFlow()

    private val rawRiwayat = combine(_startDate, _endDate) { start, end ->
        start to end
    }.flatMapLatest { (start, end) ->
        transaksiRepository.getRiwayatByDateRange(start, end)
    }.map { list ->
        list.map { r ->
            ExportRiwayat(
                tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date(r.createdAt)),
                namaObat = r.obatId.toString(),
                jenis = r.jenis,
                qty = r.qty,
                harga = r.harga,
                catatan = r.catatan
            )
        }
    }

    val riwayatWithNames = combine(
        rawRiwayat,
        obatRepository.getAllObat()
    ) { riwayatList, obatList ->
        val obatMap = obatList.associateBy { it.id }
        riwayatList.map { r ->
            val obatId = r.namaObat.toIntOrNull() ?: 0
            val namaObat = obatMap[obatId]?.nama ?: "Obat #$obatId"
            r.copy(namaObat = namaObat)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setStartDate(millis: Long) {
        _startDate.value = millis
    }

    fun setEndDate(millis: Long) {
        _endDate.value = millis
    }

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    fun export(format: String, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentData = riwayatWithNames.value
            if (currentData.isEmpty()) {
                _exportMessage.emit("Tidak ada data untuk diexport")
                return@launch
            }
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val startStr = dateFormat.format(Date(_startDate.value))
                val endStr = dateFormat.format(Date(_endDate.value))
                val file = if (format == "PDF") {
                    PdfExporter(context).exportRiwayat(currentData, startStr, endStr)
                } else {
                    ExcelExporter(context).exportRiwayat(currentData, startStr, endStr)
                }
                _exportMessage.emit("Laporan tersimpan: ${file.name}")
            } catch (e: Exception) {
                _exportMessage.emit("Gagal: ${e.message}")
            }
        }
    }
}
