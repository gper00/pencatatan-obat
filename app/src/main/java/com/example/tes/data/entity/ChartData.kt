package com.example.tes.data.entity

data class KategoriStok(
    val namaKategori: String,
    val totalStok: Int
)

data class TrenHari(
    val tanggal: String,
    val masuk: Int,
    val keluar: Int
)
