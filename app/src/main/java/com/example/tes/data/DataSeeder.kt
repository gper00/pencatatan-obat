package com.example.tes.data

import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository

class DataSeeder(
    private val obatRepository: ObatRepository,
    private val transaksiRepository: TransaksiRepository
) {
    suspend fun seed() {
        if (obatRepository.getObatById(1) != null) return

        val kategoris = obatRepository.getKategoris().associateBy { it.nama }
        val satuans = obatRepository.getSatuans().associateBy { it.nama }

        val strip = satuans["Strip"]!!.id
        val botol = satuans["Botol"]!!.id
        val sachet = satuans["Sachet"]!!.id

        val analgesik = kategoris["Analgesik"]!!.id
        val antibiotik = kategoris["Antibiotik"]!!.id
        val vitamin = kategoris["Vitamin"]!!.id
        val obatBatuk = kategoris["Obat Batuk"]!!.id
        val obatLambung = kategoris["Obat Lambung"]!!.id

        // ==================== OBAT ====================
        val obat1 = obatRepository.insertObat(Obat(
            kode = "OBT001", nama = "Paracetamol 500mg",
            kategoriId = analgesik, satuanId = strip, stok = 48,
            stokMinimum = 10, hargaBeli = 8500, hargaJual = 15000,
            expiredDate = "2027-08-15", deskripsi = "Obat penurun panas dan pereda nyeri"
        )).toInt()

        val obat2 = obatRepository.insertObat(Obat(
            kode = "OBT002", nama = "Ibuprofen 400mg",
            kategoriId = analgesik, satuanId = strip, stok = 22,
            stokMinimum = 5, hargaBeli = 12000, hargaJual = 22000,
            expiredDate = "2027-06-20", deskripsi = "Antiinflamasi non-steroid"
        )).toInt()

        val obat3 = obatRepository.insertObat(Obat(
            kode = "OBT003", nama = "Amoxicillin 500mg",
            kategoriId = antibiotik, satuanId = strip, stok = 15,
            stokMinimum = 5, hargaBeli = 18000, hargaJual = 30000,
            expiredDate = "2026-12-01", deskripsi = "Antibiotik spektrum luas"
        )).toInt()

        val obat4 = obatRepository.insertObat(Obat(
            kode = "OBT004", nama = "Ciprofloxacin 500mg",
            kategoriId = antibiotik, satuanId = strip, stok = 8,
            stokMinimum = 3, hargaBeli = 25000, hargaJual = 45000,
            expiredDate = "2027-03-10", deskripsi = "Antibiotik golongan fluorokuinolon"
        )).toInt()

        val obat5 = obatRepository.insertObat(Obat(
            kode = "OBT005", nama = "Vitamin C 500mg",
            kategoriId = vitamin, satuanId = botol, stok = 12,
            stokMinimum = 5, hargaBeli = 15000, hargaJual = 28000,
            expiredDate = "2028-01-20", deskripsi = "Suplemen vitamin C"
        )).toInt()

        val obat6 = obatRepository.insertObat(Obat(
            kode = "OBT006", nama = "Vitamin B Complex",
            kategoriId = vitamin, satuanId = botol, stok = 7,
            stokMinimum = 5, hargaBeli = 18000, hargaJual = 32000,
            expiredDate = "2027-11-05", deskripsi = "Vitamin B1, B6, B12"
        )).toInt()

        val obat7 = obatRepository.insertObat(Obat(
            kode = "OBT007", nama = "OBH Combi Batuk Berdahak",
            kategoriId = obatBatuk, satuanId = botol, stok = 3,
            stokMinimum = 10, hargaBeli = 12000, hargaJual = 22000,
            expiredDate = "2026-09-15", deskripsi = "Obat batuk berdahak menthol"
        )).toInt()

        val obat8 = obatRepository.insertObat(Obat(
            kode = "OBT008", nama = "Antangin JRG",
            kategoriId = obatBatuk, satuanId = sachet, stok = 25,
            stokMinimum = 10, hargaBeli = 2000, hargaJual = 4000,
            expiredDate = "2027-05-30", deskripsi = "Obat batuk masuk angin alami"
        )).toInt()

        val obat9 = obatRepository.insertObat(Obat(
            kode = "OBT009", nama = "Antasida DOEN",
            kategoriId = obatLambung, satuanId = strip, stok = 35,
            stokMinimum = 10, hargaBeli = 5000, hargaJual = 10000,
            expiredDate = "2027-07-22", deskripsi = "Obat maag dan asam lambung"
        )).toInt()

        val obat10 = obatRepository.insertObat(Obat(
            kode = "OBT010", nama = "Omeprazole 20mg",
            kategoriId = obatLambung, satuanId = strip, stok = 5,
            stokMinimum = 5, hargaBeli = 22000, hargaJual = 38000,
            expiredDate = "2026-08-10", deskripsi = "Penghambat pompa proton untuk GERD"
        )).toInt()

        // ==================== RIWAYAT STOK ====================

        // Pembelian awal (30 hari lalu)
        transaksiRepository.stokMasuk(obat1, 50, 8500, "Pembelian awal dari PBF")
        transaksiRepository.stokMasuk(obat2, 25, 12000, "Pembelian awal dari PBF")
        transaksiRepository.stokMasuk(obat3, 20, 18000, "Pembelian awal dari PBF")
        transaksiRepository.stokMasuk(obat4, 10, 25000, "Pembelian awal dari PBF")
        transaksiRepository.stokMasuk(obat5, 15, 15000, "Pembelian awal dari PBF")
        transaksiRepository.stokMasuk(obat6, 10, 18000, "Pembelian awal")
        transaksiRepository.stokMasuk(obat7, 20, 12000, "Pembelian awal")
        transaksiRepository.stokMasuk(obat8, 30, 2000, "Pembelian awal")
        transaksiRepository.stokMasuk(obat9, 40, 5000, "Pembelian awal")
        transaksiRepository.stokMasuk(obat10, 8, 22000, "Pembelian awal")

        // Penjualan
        transaksiRepository.stokKeluar(obat1, 5, 15000, "Terjual ke langganan")
        transaksiRepository.stokKeluar(obat1, 3, 15000, "Terjual")
        transaksiRepository.stokKeluar(obat2, 3, 22000, "Terjual")
        transaksiRepository.stokKeluar(obat3, 5, 30000, "Terjual")
        transaksiRepository.stokKeluar(obat5, 3, 28000, "Terjual")
        transaksiRepository.stokKeluar(obat7, 12, 22000, "Terjual")
        transaksiRepository.stokKeluar(obat8, 5, 4000, "Terjual")
        transaksiRepository.stokKeluar(obat9, 5, 10000, "Terjual")
        transaksiRepository.stokKeluar(obat10, 3, 38000, "Terjual")

        // Restok
        transaksiRepository.stokMasuk(obat7, 5, 12000, "Restok stok menipis")
        transaksiRepository.stokMasuk(obat4, 5, 25000, "Restok")
        transaksiRepository.stokMasuk(obat2, 10, 12000, "Restok")
        transaksiRepository.stokMasuk(obat1, 10, 8500, "Restok")
        transaksiRepository.stokMasuk(obat7, 5, 12000, "Restok")
    }
}
