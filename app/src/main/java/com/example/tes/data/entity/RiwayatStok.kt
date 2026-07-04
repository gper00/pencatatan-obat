package com.example.tes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "riwayat_stok",
    foreignKeys = [
        ForeignKey(
            entity = Obat::class,
            parentColumns = ["id"],
            childColumns = ["obat_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["obat_id"])]
)
data class RiwayatStok(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "obat_id") val obatId: Int,
    @ColumnInfo(name = "jenis") val jenis: String,
    @ColumnInfo(name = "qty") val qty: Int,
    @ColumnInfo(name = "harga") val harga: Int? = null,
    @ColumnInfo(name = "catatan") val catatan: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
