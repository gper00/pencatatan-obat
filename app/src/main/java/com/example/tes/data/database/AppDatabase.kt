package com.example.tes.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tes.data.dao.KategoriDao
import com.example.tes.data.dao.ObatDao
import com.example.tes.data.dao.RiwayatStokDao
import com.example.tes.data.dao.SatuanDao
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.entity.Satuan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Kategori::class, Satuan::class, Obat::class, RiwayatStok::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun obatDao(): ObatDao
    abstract fun kategoriDao(): KategoriDao
    abstract fun satuanDao(): SatuanDao
    abstract fun riwayatStokDao(): RiwayatStokDao

    companion object {
        private const val DB_NAME = "pencatatan_obat.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        seedDataAsync(context)
                    }
                })
                .build()
        }

        private fun seedDataAsync(context: Context) {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME).build()
            CoroutineScope(Dispatchers.IO).launch {
                db.kategoriDao().insert(Kategori(nama = "Analgesik"))
                db.kategoriDao().insert(Kategori(nama = "Antibiotik"))
                db.kategoriDao().insert(Kategori(nama = "Vitamin"))
                db.kategoriDao().insert(Kategori(nama = "Obat Batuk"))
                db.kategoriDao().insert(Kategori(nama = "Obat Lambung"))

                db.satuanDao().insert(Satuan(nama = "Box"))
                db.satuanDao().insert(Satuan(nama = "Strip"))
                db.satuanDao().insert(Satuan(nama = "Botol"))
                db.satuanDao().insert(Satuan(nama = "Tube"))
                db.satuanDao().insert(Satuan(nama = "Sachet"))
            }
        }
    }
}
