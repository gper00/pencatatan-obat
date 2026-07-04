package com.example.tes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "satuan", indices = [Index(value = ["nama"], unique = true)])
data class Satuan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nama") val nama: String
)
