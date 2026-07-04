package com.example.tes.di

import android.content.Context
import com.example.tes.data.DataSeeder
import com.example.tes.data.database.AppDatabase
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository

class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)

    val obatRepository = ObatRepository(
        obatDao = database.obatDao(),
        kategoriDao = database.kategoriDao(),
        satuanDao = database.satuanDao()
    )

    val transaksiRepository = TransaksiRepository(
        obatDao = database.obatDao(),
        riwayatStokDao = database.riwayatStokDao()
    )

    val dataSeeder = DataSeeder(obatRepository, transaksiRepository)
}
