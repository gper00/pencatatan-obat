package com.example.tes.data.export

import android.content.Context
import android.os.Environment
import org.dhatim.fastexcel.Workbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExcelExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportInventaris(obatList: List<ExportObat>): File {
        val file = createFile("inventaris-obat-${dateFormat.format(Date())}.xlsx")
        FileOutputStream(file).use { out ->
            val workbook = Workbook(out, "Inventaris Obat", "1.0")
            val sheet = workbook.newWorksheet("Inventaris")

            val headers = listOf("Kode", "Nama", "Kategori", "Stok", "Stok Minimum", "Harga Beli", "Harga Jual", "Expired")
            sheet.range(0, 0, 0, headers.size - 1).style().bold().fillColor("4CAF50").fontColor("FFFFFF").set()
            headers.forEachIndexed { i, h -> sheet.value(0, i, h) }

            obatList.forEachIndexed { idx, obat ->
                val row = idx + 1
                sheet.value(row, 0, obat.kode)
                sheet.value(row, 1, obat.nama)
                sheet.value(row, 2, obat.kategori)
                sheet.value(row, 3, obat.stok)
                sheet.value(row, 4, obat.stokMinimum)
                sheet.value(row, 5, obat.hargaBeli)
                sheet.value(row, 6, obat.hargaJual)
                sheet.value(row, 7, obat.expiredDate ?: "-")
            }

            sheet.setAutoFilter(0, 0, headers.size - 1)
            workbook.finish()
        }
        return file
    }

    fun exportRiwayat(
        riwayatList: List<ExportRiwayat>,
        startDate: String,
        endDate: String
    ): File {
        val file = createFile("riwayat-transaksi-${startDate}-${endDate}.xlsx")
        FileOutputStream(file).use { out ->
            val workbook = Workbook(out, "Riwayat Transaksi", "1.0")
            val sheet = workbook.newWorksheet("Riwayat")

            val headers = listOf("Tanggal", "Obat", "Jenis", "Qty", "Harga", "Total", "Catatan")
            sheet.range(0, 0, 0, headers.size - 1).style().bold().fillColor("2196F3").fontColor("FFFFFF").set()
            headers.forEachIndexed { i, h -> sheet.value(0, i, h) }

            riwayatList.forEachIndexed { idx, r ->
                val row = idx + 1
                sheet.value(row, 0, r.tanggal)
                sheet.value(row, 1, r.namaObat)
                sheet.value(row, 2, r.jenis)
                sheet.value(row, 3, r.qty)
                sheet.value(row, 4, r.harga ?: 0)
                sheet.value(row, 5, (r.harga ?: 0) * r.qty)
                sheet.value(row, 6, r.catatan ?: "-")
            }

            sheet.setAutoFilter(0, 0, headers.size - 1)
            workbook.finish()
        }
        return file
    }

    fun exportStokKritis(
        hampirHabis: List<ExportObat>,
        akanExpired: List<ExportObat>
    ): File {
        val file = createFile("laporan-stok-kritis-${dateFormat.format(Date())}.xlsx")
        FileOutputStream(file).use { out ->
            val workbook = Workbook(out, "Stok Kritis", "1.0")

            // Sheet 1: Stok Hampir Habis
            val sheet1 = workbook.newWorksheet("Stok Hampir Habis")
            val headers1 = listOf("Kode", "Nama", "Kategori", "Stok", "Stok Minimum")
            sheet1.range(0, 0, 0, headers1.size - 1).style().bold().fillColor("E53935").fontColor("FFFFFF").set()
            headers1.forEachIndexed { i, h -> sheet1.value(0, i, h) }
            hampirHabis.forEachIndexed { idx, obat ->
                val row = idx + 1
                sheet1.value(row, 0, obat.kode)
                sheet1.value(row, 1, obat.nama)
                sheet1.value(row, 2, obat.kategori)
                sheet1.value(row, 3, obat.stok)
                sheet1.value(row, 4, obat.stokMinimum)
            }

            // Sheet 2: Akan Expired
            val sheet2 = workbook.newWorksheet("Akan Expired")
            val headers2 = listOf("Kode", "Nama", "Kategori", "Stok", "Tanggal Expired")
            sheet2.range(0, 0, 0, headers2.size - 1).style().bold().fillColor("2196F3").fontColor("FFFFFF").set()
            headers2.forEachIndexed { i, h -> sheet2.value(0, i, h) }
            akanExpired.forEachIndexed { idx, obat ->
                val row = idx + 1
                sheet2.value(row, 0, obat.kode)
                sheet2.value(row, 1, obat.nama)
                sheet2.value(row, 2, obat.kategori)
                sheet2.value(row, 3, obat.stok)
                sheet2.value(row, 4, obat.expiredDate ?: "-")
            }

            workbook.finish()
        }
        return file
    }

    private fun createFile(fileName: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        return File(downloadsDir, fileName)
    }
}
