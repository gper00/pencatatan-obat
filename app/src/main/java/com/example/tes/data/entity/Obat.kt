package com.example.tes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "obat",
    foreignKeys = [
        ForeignKey(
            entity = Kategori::class,
            parentColumns = ["id"],
            childColumns = ["kategori_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Satuan::class,
            parentColumns = ["id"],
            childColumns = ["satuan_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["kode"], unique = true),
        Index(value = ["nama"]),
        Index(value = ["kategori_id"]),
        Index(value = ["satuan_id"]),
        Index(value = ["expired_date"])
    ]
)
data class Obat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "kode") val kode: String,
    @ColumnInfo(name = "nama") val nama: String,
    @ColumnInfo(name = "kategori_id") val kategoriId: Int,
    @ColumnInfo(name = "satuan_id") val satuanId: Int,
    @ColumnInfo(name = "stok") val stok: Int = 0,
    @ColumnInfo(name = "stok_minimum") val stokMinimum: Int = 5,
    @ColumnInfo(name = "harga_beli") val hargaBeli: Int,
    @ColumnInfo(name = "harga_jual") val hargaJual: Int,
    @ColumnInfo(name = "expired_date") val expiredDate: String? = null,
    @ColumnInfo(name = "deskripsi") val deskripsi: String? = null
)
