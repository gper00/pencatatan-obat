package com.example.tes.data.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportObat(
    val kode: String,
    val nama: String,
    val kategori: String,
    val stok: Int,
    val stokMinimum: Int,
    val hargaBeli: Int,
    val hargaJual: Int,
    val expiredDate: String?
)

data class ExportRiwayat(
    val tanggal: String,
    val namaObat: String,
    val jenis: String,
    val qty: Int,
    val harga: Int?,
    val catatan: String?
)

class PdfExporter(private val context: Context) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val currencyFormat = NumberFormat.getInstance(Locale("id", "ID"))

    fun exportInventaris(obatList: List<ExportObat>): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.DKGRAY
        }
        val headerPaint = Paint().apply {
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            color = Color.WHITE
        }
        val rowPaint = Paint().apply {
            textSize = 11f
            color = Color.DKGRAY
        }
        val headerBgPaint = Paint().apply {
            color = Color.rgb(76, 175, 80)
        }
        val rowBg = Paint().apply { color = Color.rgb(245, 245, 245) }

        canvas.drawText("Inventaris Obat", 40f, 50f, titlePaint)
        canvas.drawText("Tanggal cetak: ${dateFormat.format(Date())}", 40f, 75f, rowPaint)

        val colX = floatArrayOf(40f, 110f, 200f, 270f, 320f, 400f, 470f)
        val headers = arrayOf("Kode", "Nama", "Kategori", "Stok", "Min", "Harga Jual", "Expired")
        canvas.drawRect(35f, 90f, 560f, 115f, headerBgPaint)
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, colX[i], 109f, headerPaint)
        }

        var y = 130f
        obatList.forEachIndexed { idx, obat ->
            if (y > 800f) {
                doc.finishPage(page)
                // Continue on next page if needed
            }
            if (idx % 2 == 1) canvas.drawRect(35f, y - 15f, 560f, y + 13f, rowBg)
            canvas.drawText(obat.kode, colX[0], y, rowPaint)
            canvas.drawText(obat.nama.take(15), colX[1], y, rowPaint)
            canvas.drawText(obat.kategori, colX[2], y, rowPaint)
            canvas.drawText(obat.stok.toString(), colX[3], y, rowPaint)
            canvas.drawText(obat.stokMinimum.toString(), colX[4], y, rowPaint)
            val hargaJualStr = if (obat.hargaJual > 0) currencyFormat.format(obat.hargaJual) else "-"
            canvas.drawText(hargaJualStr, colX[5], y, rowPaint)
            canvas.drawText(obat.expiredDate ?: "-", colX[6], y, rowPaint)
            y += 20f
        }

        canvas.drawText(
            "Dicetak dari Aplikasi Pencatatan Obat",
            40f, 830f, rowPaint
        )

        doc.finishPage(page)
        return saveDocument(doc, "inventaris-obat-${dateFormat.format(Date())}.pdf")
    }

    fun exportRiwayat(
        riwayatList: List<ExportRiwayat>,
        startDate: String,
        endDate: String
    ): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f; typeface = Typeface.DEFAULT_BOLD; color = Color.DKGRAY
        }
        val headerPaint = Paint().apply {
            textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = Color.WHITE
        }
        val rowPaint = Paint().apply { textSize = 11f; color = Color.DKGRAY }
        val headerBgPaint = Paint().apply { color = Color.rgb(33, 150, 243) }
        val rowBg = Paint().apply { color = Color.rgb(245, 245, 245) }

        canvas.drawText("Riwayat Transaksi", 40f, 50f, titlePaint)
        canvas.drawText("Periode: $startDate s/d $endDate", 40f, 75f, rowPaint)

        val colX = floatArrayOf(40f, 120f, 200f, 250f, 310f, 380f, 440f)
        val headers = arrayOf("Tanggal", "Obat", "Jenis", "Qty", "Harga", "Total", "Catatan")
        canvas.drawRect(35f, 90f, 560f, 115f, headerBgPaint)
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, colX[i], 109f, headerPaint)
        }

        var y = 130f
        riwayatList.forEachIndexed { idx, r ->
            if (idx % 2 == 1) canvas.drawRect(35f, y - 15f, 560f, y + 13f, rowBg)
            canvas.drawText(r.tanggal, colX[0], y, rowPaint)
            canvas.drawText(r.namaObat.take(12), colX[1], y, rowPaint)
            canvas.drawText(r.jenis, colX[2], y, rowPaint)
            canvas.drawText(r.qty.toString(), colX[3], y, rowPaint)
            val hargaStr = if (r.harga != null && r.harga > 0) currencyFormat.format(r.harga) else "-"
            canvas.drawText(hargaStr, colX[4], y, rowPaint)
            val totalStr = if (r.harga != null) currencyFormat.format(r.harga * r.qty) else "-"
            canvas.drawText(totalStr, colX[5], y, rowPaint)
            canvas.drawText((r.catatan ?: "-").take(10), colX[6], y, rowPaint)
            y += 20f
        }

        canvas.drawText(
            "Dicetak dari Aplikasi Pencatatan Obat",
            40f, 830f, rowPaint
        )

        doc.finishPage(page)
        return saveDocument(doc, "riwayat-transaksi-${dateFormat.format(Date())}.pdf")
    }

    fun exportStokKritis(
        hampirHabis: List<ExportObat>,
        akanExpired: List<ExportObat>
    ): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            textSize = 22f; typeface = Typeface.DEFAULT_BOLD; color = Color.DKGRAY
        }
        val sectionPaint = Paint().apply {
            textSize = 15f; typeface = Typeface.DEFAULT_BOLD
        }
        val headerPaint = Paint().apply {
            textSize = 13f; typeface = Typeface.DEFAULT_BOLD; color = Color.WHITE
        }
        val rowPaint = Paint().apply { textSize = 11f; color = Color.DKGRAY }
        val rowBg = Paint().apply { color = Color.rgb(245, 245, 245) }

        canvas.drawText("Laporan Stok Kritis", 40f, 50f, titlePaint)
        canvas.drawText("Tanggal: ${dateFormat.format(Date())}", 40f, 75f, rowPaint)

        // Section 1: Stok Hampir Habis
        var y = 110f
        sectionPaint.color = Color.rgb(229, 57, 53)
        canvas.drawText("Stok Hampir Habis", 40f, y, sectionPaint)
        y += 20f

        val headerBgRed = Paint().apply { color = Color.rgb(229, 57, 53) }
        val colX = floatArrayOf(40f, 130f, 220f, 280f, 350f)
        val headers = arrayOf("Kode", "Nama", "Stok", "Minimum", "Kategori")
        canvas.drawRect(35f, y - 5f, 520f, y + 18f, headerBgRed)
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, colX[i], y + 14f, headerPaint)
        }
        y += 28f
        hampirHabis.forEachIndexed { idx, obat ->
            if (idx % 2 == 1) canvas.drawRect(35f, y - 5f, 520f, y + 15f, rowBg)
            canvas.drawText(obat.kode, colX[0], y + 12f, rowPaint)
            canvas.drawText(obat.nama.take(15), colX[1], y + 12f, rowPaint)
            canvas.drawText(obat.stok.toString(), colX[2], y + 12f, rowPaint)
            canvas.drawText(obat.stokMinimum.toString(), colX[3], y + 12f, rowPaint)
            canvas.drawText(obat.kategori, colX[4], y + 12f, rowPaint)
            y += 22f
        }

        y += 12f
        // Section 2: Akan Expired
        sectionPaint.color = Color.rgb(33, 150, 243)
        canvas.drawText("Akan Expired (30 Hari)", 40f, y, sectionPaint)
        y += 20f

        val headerBgBlue = Paint().apply { color = Color.rgb(33, 150, 243) }
        val colX2 = floatArrayOf(40f, 130f, 220f, 300f, 400f)
        val headers2 = arrayOf("Kode", "Nama", "Stok", "Expired", "Kategori")
        canvas.drawRect(35f, y - 5f, 520f, y + 18f, headerBgBlue)
        headers2.forEachIndexed { i, h ->
            canvas.drawText(h, colX2[i], y + 14f, headerPaint)
        }
        y += 28f
        akanExpired.forEachIndexed { idx, obat ->
            if (idx % 2 == 1) canvas.drawRect(35f, y - 5f, 520f, y + 15f, rowBg)
            canvas.drawText(obat.kode, colX2[0], y + 12f, rowPaint)
            canvas.drawText(obat.nama.take(15), colX2[1], y + 12f, rowPaint)
            canvas.drawText(obat.stok.toString(), colX2[2], y + 12f, rowPaint)
            canvas.drawText(obat.expiredDate ?: "-", colX2[3], y + 12f, rowPaint)
            canvas.drawText(obat.kategori, colX2[4], y + 12f, rowPaint)
            y += 22f
        }

        canvas.drawText(
            "Dicetak dari Aplikasi Pencatatan Obat",
            40f, 830f, rowPaint
        )

        doc.finishPage(page)
        return saveDocument(doc, "laporan-stok-kritis-${dateFormat.format(Date())}.pdf")
    }

    private fun saveDocument(doc: PdfDocument, fileName: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return file
    }
}
