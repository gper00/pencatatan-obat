# Pencatatan Obat Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an Android native medicine stock recording app (Kotlin + Jetpack Compose + Room) for single-user to manage medicine inventory, stock in/out transactions, and expiry reminders.

**Architecture:** MVVM with manual DI. UI layer uses Jetpack Compose + Material 3 + Navigation Compose. Data layer uses Room (SQLite) with Repository pattern. Single-activity app with Bottom Navigation (4 tabs: Home, Obat, Transaksi, Reminder).

**Tech Stack:** Kotlin 1.9.22, AGP 8.2.0, Jetpack Compose (BOM 2024.02), Material 3, Navigation Compose 2.7.7, Room 2.6.1, KSP, Gradle 8.2

## Global Constraints

- Package name: `com.example.tes` (existing, keep as-is)
- Min SDK 24, Target SDK 34, compileSdk 34
- All prices in Indonesian Rupiah (Integer, not Float)
- All monetary values stored as Integer (not Long)
- Room version 2.6.1 with KSP (not kapt)
- Kotlin 1.9.22, AGP 8.2.0
- Manual DI (no Hilt/Koin)
- Jetpack Compose (no XML for new screens, but keep existing XML intact until migration)
- All strings in Indonesian language
- Use `StateFlow` not `LiveData` for ViewModel state

---

### Task 1: Gradle Setup — Compose + Room + Navigation

**Files:**
- Modify: `build.gradle.kts` (root)
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Produces: Build configuration with Compose, Room, Navigation, KSP enabled

- [ ] **Step 1: Update root build.gradle.kts with KSP plugin**

Replace content of `build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

- [ ] **Step 2: Update app/build.gradle.kts with all dependencies**

Replace the entire `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.tes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tes"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Material (for existing XML layout)
    implementation("com.google.android.material:material:1.11.0")
}
```

- [ ] **Step 3: Verify Gradle sync compiles**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 2: Data Layer — Entities

**Files:**
- Create: `app/src/main/java/com/example/tes/data/entity/Obat.kt`
- Create: `app/src/main/java/com/example/tes/data/entity/Kategori.kt`
- Create: `app/src/main/java/com/example/tes/data/entity/Satuan.kt`
- Create: `app/src/main/java/com/example/tes/data/entity/RiwayatStok.kt`

**Interfaces:**
- Produces: 4 Room `@Entity` classes consumed by DAOs in Task 3

- [ ] **Step 1: Create `Kategori.kt`**

```kotlin
package com.example.tes.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "kategori", indices = [Index(value = ["nama"], unique = true)])
data class Kategori(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "nama") val nama: String
)
```

- [ ] **Step 2: Create `Satuan.kt`**

```kotlin
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
```

- [ ] **Step 3: Create `Obat.kt`**

```kotlin
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
```

- [ ] **Step 4: Create `RiwayatStok.kt`**

```kotlin
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
    @ColumnInfo(name = "jenis") val jenis: String, // MASUK / KELUAR / PENYESUAIAN
    @ColumnInfo(name = "qty") val qty: Int,
    @ColumnInfo(name = "harga") val harga: Int? = null,
    @ColumnInfo(name = "catatan") val catatan: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 5: Create wrapper classes for queries with joins**

Create `app/src/main/java/com/example/tes/data/entity/ObatWithRelations.kt`:

```kotlin
package com.example.tes.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ObatWithKategori(
    @Embedded val obat: Obat,
    @Relation(parentColumn = "kategori_id", entityColumn = "id")
    val kategori: List<Kategori>
)

data class ObatWithSatuan(
    @Embedded val obat: Obat,
    @Relation(parentColumn = "satuan_id", entityColumn = "id")
    val satuan: List<Satuan>
)

data class RiwayatWithObat(
    @Embedded val riwayat: RiwayatStok,
    @Relation(parentColumn = "obat_id", entityColumn = "id")
    val obat: List<Obat>
)
```

---

### Task 3: Data Layer — DAOs

**Files:**
- Create: `app/src/main/java/com/example/tes/data/dao/ObatDao.kt`
- Create: `app/src/main/java/com/example/tes/data/dao/KategoriDao.kt`
- Create: `app/src/main/java/com/example/tes/data/dao/SatuanDao.kt`
- Create: `app/src/main/java/com/example/tes/data/dao/RiwayatStokDao.kt`

**Interfaces:**
- Consumes: Entities from Task 2
- Produces: DAO interfaces consumed by AppDatabase in Task 4 and Repositories in Task 5

- [ ] **Step 1: Create `KategoriDao.kt`**

```kotlin
package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tes.data.entity.Kategori
import kotlinx.coroutines.flow.Flow

@Dao
interface KategoriDao {
    @Query("SELECT * FROM kategori ORDER BY nama ASC")
    fun getAll(): Flow<List<Kategori>>

    @Query("SELECT * FROM kategori ORDER BY nama ASC")
    suspend fun getAllOnce(): List<Kategori>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(kategori: Kategori): Long

    @Delete
    suspend fun delete(kategori: Kategori)

    @Query("SELECT COUNT(*) FROM kategori")
    suspend fun count(): Int
}
```

- [ ] **Step 2: Create `SatuanDao.kt`**

```kotlin
package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tes.data.entity.Satuan
import kotlinx.coroutines.flow.Flow

@Dao
interface SatuanDao {
    @Query("SELECT * FROM satuan ORDER BY nama ASC")
    fun getAll(): Flow<List<Satuan>>

    @Query("SELECT * FROM satuan ORDER BY nama ASC")
    suspend fun getAllOnce(): List<Satuan>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(satuan: Satuan): Long

    @Delete
    suspend fun delete(satuan: Satuan)

    @Query("SELECT COUNT(*) FROM satuan")
    suspend fun count(): Int
}
```

- [ ] **Step 3: Create `ObatDao.kt`**

```kotlin
package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tes.data.entity.Obat
import kotlinx.coroutines.flow.Flow

@Dao
interface ObatDao {
    @Query("SELECT * FROM obat ORDER BY nama ASC")
    fun getAll(): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE id = :id")
    suspend fun getById(id: Int): Obat?

    @Query("SELECT * FROM obat WHERE nama LIKE '%' || :query || '%' OR kode LIKE '%' || :query || '%' ORDER BY nama ASC")
    fun search(query: String): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE stok <= stok_minimum ORDER BY stok ASC")
    fun getStokMenipis(): Flow<List<Obat>>

    @Query("SELECT * FROM obat WHERE expired_date IS NOT NULL AND expired_date <= :tgl ORDER BY expired_date ASC")
    fun getAkanExpired(tgl: String): Flow<List<Obat>>

    @Query("SELECT COUNT(*) FROM obat")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM obat WHERE stok <= stok_minimum")
    fun countStokMenipis(): Flow<Int>

    @Query("SELECT COUNT(*) FROM obat WHERE expired_date IS NOT NULL AND expired_date <= :tgl")
    fun countAkanExpired(tgl: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(obat: Obat): Long

    @Update
    suspend fun update(obat: Obat)

    @Delete
    suspend fun delete(obat: Obat)

    @Query("UPDATE obat SET stok = :stok WHERE id = :id")
    suspend fun updateStok(id: Int, stok: Int)
}
```

- [ ] **Step 4: Create `RiwayatStokDao.kt`**

```kotlin
package com.example.tes.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tes.data.entity.RiwayatStok
import kotlinx.coroutines.flow.Flow

@Dao
interface RiwayatStokDao {
    @Query("SELECT * FROM riwayat_stok WHERE obat_id = :obatId ORDER BY created_at DESC")
    fun getByObatId(obatId: Int): Flow<List<RiwayatStok>>

    @Query("SELECT * FROM riwayat_stok ORDER BY created_at DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<RiwayatStok>>

    @Query("""
        SELECT * FROM riwayat_stok 
        WHERE obat_id = :obatId 
        ORDER BY created_at DESC 
        LIMIT :limit
    """)
    fun getByObatIdLimit(obatId: Int, limit: Int = 10): Flow<List<RiwayatStok>>

    @Insert
    suspend fun insert(riwayat: RiwayatStok): Long
}
```

---

### Task 4: Data Layer — Database + Seed Data

**Files:**
- Create: `app/src/main/java/com/example/tes/data/database/AppDatabase.kt`

**Interfaces:**
- Consumes: DAOs from Task 3
- Produces: `AppDatabase` instance consumed by AppContainer in Task 6

- [ ] **Step 1: Create `AppDatabase.kt`**

```kotlin
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
                        seedData(context)
                    }
                })
                .build()
        }

        private fun seedData(context: Context) {
            val db = Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .build()
            CoroutineScope(Dispatchers.IO).launch {
                // Seed kategori
                db.kategoriDao().insert(Kategori(nama = "Analgesik"))
                db.kategoriDao().insert(Kategori(nama = "Antibiotik"))
                db.kategoriDao().insert(Kategori(nama = "Vitamin"))
                db.kategoriDao().insert(Kategori(nama = "Obat Batuk"))
                db.kategoriDao().insert(Kategori(nama = "Obat Lambung"))

                // Seed satuan
                db.satuanDao().insert(Satuan(nama = "Box"))
                db.satuanDao().insert(Satuan(nama = "Strip"))
                db.satuanDao().insert(Satuan(nama = "Botol"))
                db.satuanDao().insert(Satuan(nama = "Tube"))
                db.satuanDao().insert(Satuan(nama = "Sachet"))
            }
        }
    }
}
```

---

### Task 5: Repository Layer

**Files:**
- Create: `app/src/main/java/com/example/tes/data/repository/ObatRepository.kt`
- Create: `app/src/main/java/com/example/tes/data/repository/TransaksiRepository.kt`

**Interfaces:**
- Consumes: DAOs from Task 3
- Produces: Repository classes consumed by ViewModels in Task 8-12

- [ ] **Step 1: Create `ObatRepository.kt`**

```kotlin
package com.example.tes.data.repository

import androidx.room.Transaction
import com.example.tes.data.dao.KategoriDao
import com.example.tes.data.dao.ObatDao
import com.example.tes.data.dao.RiwayatStokDao
import com.example.tes.data.dao.SatuanDao
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.entity.Satuan
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ObatRepository(
    private val obatDao: ObatDao,
    private val kategoriDao: KategoriDao,
    private val satuanDao: SatuanDao
) {
    fun getAllObat(): Flow<List<Obat>> = obatDao.getAll()

    fun searchObat(query: String): Flow<List<Obat>> = obatDao.search(query)

    suspend fun getObatById(id: Int): Obat? = obatDao.getById(id)

    suspend fun insertObat(obat: Obat): Long = obatDao.insert(obat)

    suspend fun updateObat(obat: Obat) = obatDao.update(obat)

    suspend fun deleteObat(obat: Obat) = obatDao.delete(obat)

    suspend fun getKategoris(): List<Kategori> = kategoriDao.getAllOnce()

    fun getAllKategoris(): Flow<List<Kategori>> = kategoriDao.getAll()

    suspend fun getSatuans(): List<Satuan> = satuanDao.getAllOnce()

    fun getAllSatuans(): Flow<List<Satuan>> = satuanDao.getAll()

    fun getStokMenipis(): Flow<List<Obat>> = obatDao.getStokMenipis()

    fun getAkanExpired(): Flow<List<Obat>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        val tgl = dateFormat.format(cal.time)
        return obatDao.getAkanExpired(tgl)
    }

    fun countAll(): Flow<Int> = obatDao.countAll()
    fun countStokMenipis(): Flow<Int> = obatDao.countStokMenipis()
    fun countAkanExpired(): Flow<Int> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 30)
        val tgl = dateFormat.format(cal.time)
        return obatDao.countAkanExpired(tgl)
    }
}
```

- [ ] **Step 2: Create `TransaksiRepository.kt`**

```kotlin
package com.example.tes.data.repository

import com.example.tes.data.dao.ObatDao
import com.example.tes.data.dao.RiwayatStokDao
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import kotlinx.coroutines.flow.Flow

class TransaksiRepository(
    private val obatDao: ObatDao,
    private val riwayatStokDao: RiwayatStokDao
) {
    fun getRiwayatByObat(obatId: Int): Flow<List<RiwayatStok>> =
        riwayatStokDao.getByObatId(obatId)

    fun getRiwayatByObatLimit(obatId: Int, limit: Int = 10): Flow<List<RiwayatStok>> =
        riwayatStokDao.getByObatIdLimit(obatId, limit)

    fun getRecentRiwayat(limit: Int = 50): Flow<List<RiwayatStok>> =
        riwayatStokDao.getRecent(limit)

    suspend fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?) {
        val obat = obatDao.getById(obatId) ?: return
        val stokBaru = obat.stok + qty
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "MASUK",
                qty = qty,
                harga = harga,
                catatan = catatan
            )
        )
    }

    suspend fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?): Boolean {
        val obat = obatDao.getById(obatId) ?: return false
        if (obat.stok < qty) return false
        val stokBaru = obat.stok - qty
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "KELUAR",
                qty = qty,
                harga = harga,
                catatan = catatan
            )
        )
        return true
    }

    suspend fun penyesuaianStok(obatId: Int, stokBaru: Int, catatan: String?) {
        val obat = obatDao.getById(obatId) ?: return
        obatDao.updateStok(obatId, stokBaru)
        riwayatStokDao.insert(
            RiwayatStok(
                obatId = obatId,
                jenis = "PENYESUAIAN",
                qty = stokBaru,
                catatan = catatan
            )
        )
    }
}
```

---

### Task 6: DI Container + Application Class

**Files:**
- Create: `app/src/main/java/com/example/tes/di/AppContainer.kt`
- Create: `app/src/main/java/com/example/tes/TesApplication.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: AppDatabase from Task 4, Repositories from Task 5
- Produces: AppContainer with repo instances consumed by ViewModels

- [ ] **Step 1: Create `AppContainer.kt`**

```kotlin
package com.example.tes.di

import android.content.Context
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
}
```

- [ ] **Step 2: Create `TesApplication.kt`**

```kotlin
package com.example.tes

import android.app.Application
import com.example.tes.di.AppContainer

class TesApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
```

- [ ] **Step 3: Update `AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".TesApplication"
        android:label="Pencatatan Obat"
        android:theme="@style/Theme.AppCompat.Light">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

---

### Task 7: UI Theme — Material 3

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/theme/Color.kt`
- Create: `app/src/main/java/com/example/tes/ui/theme/Type.kt`
- Create: `app/src/main/java/com/example/tes/ui/theme/Theme.kt`

**Interfaces:**
- Produces: Material 3 theme consumed by all Compose screens

- [ ] **Step 1: Create `Color.kt`**

```kotlin
package com.example.tes.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors
val md_theme_light_primary = Color(0xFF156E4B)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA7F7CB)
val md_theme_light_onPrimaryContainer = Color(0xFF002114)
val md_theme_light_secondary = Color(0xFF4D6357)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCFE9D9)
val md_theme_light_onSecondaryContainer = Color(0xFF0A1F16)
val md_theme_light_tertiary = Color(0xFF3D6472)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_background = Color(0xFFFBFDF8)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF8)
val md_theme_light_onSurface = Color(0xFF191C1A)
val md_theme_light_surfaceVariant = Color(0xFFDCE5DC)
val md_theme_light_onSurfaceVariant = Color(0xFF414942)

// Dark theme colors
val md_theme_dark_primary = Color(0xFF6EDA9F)
val md_theme_dark_onPrimary = Color(0xFF003825)
val md_theme_dark_primaryContainer = Color(0xFF005236)
val md_theme_dark_onPrimaryContainer = Color(0xFFA7F7CB)
val md_theme_dark_secondary = Color(0xFFB4CCBE)
val md_theme_dark_onSecondary = Color(0xFF1F352B)
val md_theme_dark_secondaryContainer = Color(0xFF354B40)
val md_theme_dark_onSecondaryContainer = Color(0xFFCFE9D9)
val md_theme_dark_tertiary = Color(0xFF93CDDD)
val md_theme_dark_onTertiary = Color(0xFF003240)
val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
val md_theme_dark_surfaceVariant = Color(0xFF414942)
val md_theme_dark_onSurfaceVariant = Color(0xFFC1C9C0)
```

- [ ] **Step 2: Create `Type.kt`**

```kotlin
package com.example.tes.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

- [ ] **Step 3: Create `Theme.kt`**

```kotlin
package com.example.tes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
)

@Composable
fun TesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

---

### Task 8: Navigation — Bottom Navigation + NavHost

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/navigation/BottomNavItem.kt`
- Create: `app/src/main/java/com/example/tes/ui/navigation/NavGraph.kt`
- Rewrite: `app/src/main/java/com/example/tes/MainActivity.kt`

**Interfaces:**
- Consumes: All screens (will be created in later tasks — uses string routes for now)
- Produces: Main navigation shell with bottom tabs

- [ ] **Step 1: Create `BottomNavItem.kt`**

```kotlin
package com.example.tes.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", "Beranda", Icons.Default.Home)
    object Obat : BottomNavItem("obat", "Obat", Icons.Default.Medication)
    object Transaksi : BottomNavItem("transaksi", "Transaksi", Icons.Default.SwapHoriz)
    object Reminder : BottomNavItem("reminder", "Pengingat", Icons.Default.Notifications)
}

object Routes {
    const val HOME = "home"
    const val OBAT_LIST = "obat"
    const val OBAT_FORM = "obat/form?obatId={obatId}"
    const val OBAT_DETAIL = "obat/{obatId}"
    const val TRANSAKSI = "transaksi"
    const val RIWAYAT = "transaksi/riwayat/{obatId}"
    const val REMINDER = "reminder"

    fun obatDetail(obatId: Int) = "obat/$obatId"
    fun obatForm(obatId: Int? = null) = if (obatId != null) "obat/form?obatId=$obatId" else "obat/form"
    fun riwayat(obatId: Int) = "transaksi/riwayat/$obatId"
}
```

- [ ] **Step 2: Create `NavGraph.kt`**

```kotlin
package com.example.tes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            // TODO: HomeScreen
        }
        composable(Routes.OBAT_LIST) {
            // TODO: ObatListScreen
        }
        composable(
            route = Routes.OBAT_FORM,
            arguments = listOf(navArgument("obatId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val obatId = backStackEntry.arguments?.getInt("obatId") ?: -1
            // TODO: ObatFormScreen(obatId = if (obatId == -1) null else obatId)
        }
        composable(
            route = Routes.OBAT_DETAIL,
            arguments = listOf(navArgument("obatId") { type = NavType.IntType })
        ) { backStackEntry ->
            val obatId = backStackEntry.arguments?.getInt("obatId") ?: return@composable
            // TODO: ObatDetailScreen(obatId)
        }
        composable(Routes.TRANSAKSI) {
            // TODO: TransaksiScreen
        }
        composable(
            route = Routes.RIWAYAT,
            arguments = listOf(navArgument("obatId") { type = NavType.IntType })
        ) { backStackEntry ->
            val obatId = backStackEntry.arguments?.getInt("obatId") ?: return@composable
            // TODO: RiwayatScreen(obatId)
        }
        composable(Routes.REMINDER) {
            // TODO: ReminderScreen
        }
    }
}
```

- [ ] **Step 3: Rewrite `MainActivity.kt`**

```kotlin
package com.example.tes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tes.ui.navigation.BottomNavItem
import com.example.tes.ui.navigation.NavGraph
import com.example.tes.ui.theme.TesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TesTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Obat,
        BottomNavItem.Transaksi,
        BottomNavItem.Reminder
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
```

Also update `NavGraph.kt` to accept `modifier`:

```kotlin
@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        // ... same composable entries
    }
}
```

---

### Task 9: HomeScreen — Dashboard

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/home/HomeViewModel.kt`
- Create: `app/src/main/java/com/example/tes/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/tes/ui/navigation/NavGraph.kt` (wire HomeScreen)

**Interfaces:**
- Consumes: `ObatRepository` from AppContainer

- [ ] **Step 1: Create `HomeViewModel.kt`**

```kotlin
package com.example.tes.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.repository.ObatRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val totalObat: Int = 0,
    val stokMenipis: Int = 0,
    val akanExpired: Int = 0
)

class HomeViewModel(
    private val obatRepository: ObatRepository
) : ViewModel() {

    val totalObat: StateFlow<Int> = obatRepository.countAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stokMenipis: StateFlow<Int> = obatRepository.countStokMenipis()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val akanExpired: StateFlow<Int> = obatRepository.countAkanExpired()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
```

- [ ] **Step 2: Create `HomeScreen.kt`**

```kotlin
package com.example.tes.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val totalObat by viewModel.totalObat.collectAsState()
    val stokMenipis by viewModel.stokMenipis.collectAsState()
    val akanExpired by viewModel.akanExpired.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CheckCircle,
                label = "Total Obat",
                value = totalObat.toString(),
                color = MaterialTheme.colorScheme.primary
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Warning,
                label = "Hampir Habis",
                value = stokMenipis.toString(),
                color = MaterialTheme.colorScheme.error
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Info,
                label = "Akan Expired",
                value = akanExpired.toString(),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

- [ ] **Step 3: Update `NavGraph.kt` to wire HomeScreen**

Replace the `// TODO: HomeScreen` in NavGraph:

```kotlin
composable(Routes.HOME) {
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { HomeViewModel(app.container.obatRepository) }
    HomeScreen(viewModel = viewModel)
}
```

Add imports:
```kotlin
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.tes.TesApplication
import com.example.tes.ui.home.HomeScreen
import com.example.tes.ui.home.HomeViewModel
```

---

### Task 10: Obat Screens — List, Form, Detail

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/obat/ObatViewModel.kt`
- Create: `app/src/main/java/com/example/tes/ui/obat/ObatListScreen.kt`
- Create: `app/src/main/java/com/example/tes/ui/obat/ObatFormScreen.kt`
- Create: `app/src/main/java/com/example/tes/ui/obat/ObatDetailScreen.kt`
- Modify: `app/src/main/java/com/example/tes/ui/navigation/NavGraph.kt` (wire screens)

**Interfaces:**
- Consumes: `ObatRepository`, `TransaksiRepository` from AppContainer

This task is large — will be split into sub-steps.

- [ ] **Step 1: Create `ObatViewModel.kt`**

```kotlin
package com.example.tes.ui.obat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.Satuan
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ObatViewModel(
    private val obatRepository: ObatRepository,
    private val transaksiRepository: TransaksiRepository
) : ViewModel() {

    val obatList: StateFlow<List<Obat>> = obatRepository.getAllObat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kategoriList: StateFlow<List<Kategori>> = obatRepository.getAllKategoris()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val satuanList: StateFlow<List<Satuan>> = obatRepository.getAllSatuans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResult: StateFlow<List<Obat>> = _searchQuery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "").let { query ->
            // Reactively switch between full list and search
            // Actually let's use a simpler approach:
            MutableStateFlow(emptyList<Obat>()).asStateFlow()
        }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchObat(query: String): StateFlow<List<Obat>> {
        return obatRepository.searchObat(query)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun insertObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.insertObat(obat)
            callback()
        }
    }

    fun updateObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.updateObat(obat)
            callback()
        }
    }

    fun deleteObat(obat: Obat, callback: () -> Unit = {}) {
        viewModelScope.launch {
            obatRepository.deleteObat(obat)
            callback()
        }
    }

    // Detail state
    private val _selectedObat = MutableStateFlow<Obat?>(null)
    val selectedObat: StateFlow<Obat?> = _selectedObat.asStateFlow()

    fun loadObat(id: Int) {
        viewModelScope.launch {
            _selectedObat.value = obatRepository.getObatById(id)
        }
    }

    fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?, callback: () -> Unit = {}) {
        viewModelScope.launch {
            transaksiRepository.stokMasuk(obatId, qty, harga, catatan)
            loadObat(obatId)
            callback()
        }
    }

    fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?, callback: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = transaksiRepository.stokKeluar(obatId, qty, harga, catatan)
            if (success) loadObat(obatId)
            callback(success)
        }
    }
}
```

- [ ] **Step 2: Create `ObatListScreen.kt`**

```kotlin
package com.example.tes.ui.obat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Obat

@Composable
fun ObatListScreen(
    viewModel: ObatViewModel,
    onObatClick: (Int) -> Unit,
    onTambahObat: () -> Unit
) {
    val obatList by viewModel.obatList.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val searchResult = if (searchText.isBlank()) {
        obatList
    } else {
        viewModel.searchObat(searchText).collectAsState().value
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onTambahObat) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Obat")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Daftar Obat",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari obat...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResult, key = { it.id }) { obat ->
                    ObatListItem(
                        obat = obat,
                        onClick = { onObatClick(obat.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ObatListItem(obat: Obat, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = obat.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Kode: ${obat.kode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Stok: ${obat.stok}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (obat.stok <= obat.stokMinimum)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

- [ ] **Step 3: Create `ObatFormScreen.kt`**

```kotlin
package com.example.tes.ui.obat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Kategori
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.Satuan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObatFormScreen(
    viewModel: ObatViewModel,
    obatId: Int? = null,
    onSaved: () -> Unit
) {
    val kategoriList by viewModel.kategoriList.collectAsState()
    val satuanList by viewModel.satuanList.collectAsState()
    val selectedObat by viewModel.selectedObat.collectAsState()
    val isEdit = obatId != null

    var kode by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var selectedKategori by remember { mutableStateOf<Kategori?>(null) }
    var selectedSatuan by remember { mutableStateOf<Satuan?>(null) }
    var stok by remember { mutableStateOf("0") }
    var stokMinimum by remember { mutableStateOf("5") }
    var hargaBeli by remember { mutableStateOf("") }
    var hargaJual by remember { mutableStateOf("") }
    var expiredDate by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }

    // Load existing data for edit mode
    androidx.compose.runtime.LaunchedEffect(obatId) {
        if (obatId != null) {
            viewModel.loadObat(obatId)
        }
    }
    androidx.compose.runtime.LaunchedEffect(selectedObat) {
        if (isEdit && selectedObat != null) {
            val o = selectedObat!!
            kode = o.kode
            nama = o.nama
            stok = o.stok.toString()
            stokMinimum = o.stokMinimum.toString()
            hargaBeli = o.hargaBeli.toString()
            hargaJual = o.hargaJual.toString()
            expiredDate = o.expiredDate ?: ""
            deskripsi = o.deskripsi ?: ""
            selectedKategori = kategoriList.find { it.id == o.kategoriId }
            selectedSatuan = satuanList.find { it.id == o.satuanId }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (isEdit) "Edit Obat" else "Tambah Obat",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = kode,
            onValueChange = { kode = it },
            label = { Text("Kode Obat") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Obat") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Kategori dropdown
        var kategoriExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = kategoriExpanded,
            onExpandedChange = { kategoriExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedKategori?.nama ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategori") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = kategoriExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = kategoriExpanded,
                onDismissRequest = { kategoriExpanded = false }
            ) {
                kategoriList.forEach { kategori ->
                    DropdownMenuItem(
                        text = { Text(kategori.nama) },
                        onClick = {
                            selectedKategori = kategori
                            kategoriExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Satuan dropdown
        var satuanExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = satuanExpanded,
            onExpandedChange = { satuanExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedSatuan?.nama ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Satuan") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = satuanExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = satuanExpanded,
                onDismissRequest = { satuanExpanded = false }
            ) {
                satuanList.forEach { satuan ->
                    DropdownMenuItem(
                        text = { Text(satuan.nama) },
                        onClick = {
                            selectedSatuan = satuan
                            satuanExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!isEdit) {
            OutlinedTextField(
                value = stok,
                onValueChange = { stok = it.filter { c -> c.isDigit() } },
                label = { Text("Stok Awal") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = stokMinimum,
            onValueChange = { stokMinimum = it.filter { c -> c.isDigit() } },
            label = { Text("Stok Minimum") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = hargaBeli,
            onValueChange = { hargaBeli = it.filter { c -> c.isDigit() } },
            label = { Text("Harga Beli (Rp)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = hargaJual,
            onValueChange = { hargaJual = it.filter { c -> c.isDigit() } },
            label = { Text("Harga Jual (Rp)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = expiredDate,
            onValueChange = { expiredDate = it },
            label = { Text("Tanggal Expired (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Contoh: 2027-03-15") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = deskripsi,
            onValueChange = { deskripsi = it },
            label = { Text("Deskripsi (opsional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nama.isBlank() || kode.isBlank() || selectedKategori == null || selectedSatuan == null) return@Button
                val obat = Obat(
                    id = if (isEdit) obatId!! else 0,
                    kode = kode,
                    nama = nama,
                    kategoriId = selectedKategori!!.id,
                    satuanId = selectedSatuan!!.id,
                    stok = if (isEdit) selectedObat?.stok ?: 0 else (stok.toIntOrNull() ?: 0),
                    stokMinimum = stokMinimum.toIntOrNull() ?: 5,
                    hargaBeli = hargaBeli.toIntOrNull() ?: 0,
                    hargaJual = hargaJual.toIntOrNull() ?: 0,
                    expiredDate = expiredDate.ifBlank { null },
                    deskripsi = deskripsi.ifBlank { null }
                )
                if (isEdit) {
                    viewModel.updateObat(obat, onSaved)
                } else {
                    viewModel.insertObat(obat, onSaved)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEdit) "Simpan Perubahan" else "Tambah Obat")
        }
    }
}
```

- [ ] **Step 4: Create `ObatDetailScreen.kt`**

```kotlin
package com.example.tes.ui.obat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.repository.TransaksiRepository
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ObatDetailScreen(
    viewModel: ObatViewModel,
    transaksiRepository: TransaksiRepository,
    obatId: Int,
    onEdit: (Int) -> Unit,
    onBack: () -> Unit
) {
    val obat by viewModel.selectedObat.collectAsState()
    val riwayat by transaksiRepository.getRiwayatByObatLimit(obatId, 10)
        .collectAsState(initial = emptyList())

    LaunchedEffect(obatId) {
        viewModel.loadObat(obatId)
    }

    var showStokMasukDialog by remember { mutableStateOf(false) }
    var showStokKeluarDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getInstance(Locale("id", "ID"))

    obat?.let { o ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header info
            Text(
                text = o.nama,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Kode: ${o.kode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow("Stok", "${o.stok}", o.stok <= o.stokMinimum)
                    InfoRow("Stok Minimum", "${o.stokMinimum}")
                    InfoRow("Harga Beli", "Rp ${formatter.format(o.hargaBeli)}")
                    InfoRow("Harga Jual", "Rp ${formatter.format(o.hargaJual)}")
                    if (o.expiredDate != null) {
                        InfoRow("Expired", o.expiredDate!!)
                    }
                    if (o.deskripsi != null) {
                        InfoRow("Deskripsi", o.deskripsi!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { showStokMasukDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Stok")
                }
                FilledTonalButton(
                    onClick = { showStokKeluarDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kurangi Stok")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(o.id) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                OutlinedButton(
                    onClick = { showHapusDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Riwayat Stok",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            riwayat.forEach { r ->
                RiwayatItem(r, formatter)
            }
        }
    }

    // Dialogs
    if (showStokMasukDialog) {
        StokDialog(
            title = "Tambah Stok",
            onConfirm = { qty, harga, catatan ->
                viewModel.stokMasuk(obatId, qty, harga, catatan)
                showStokMasukDialog = false
            },
            onDismiss = { showStokMasukDialog = false }
        )
    }

    if (showStokKeluarDialog) {
        StokDialog(
            title = "Kurangi Stok",
            onConfirm = { qty, harga, catatan ->
                viewModel.stokKeluar(obatId, qty, harga, catatan) { success ->
                    if (!success) {
                        // TODO: show error toast
                    }
                }
                showStokKeluarDialog = false
            },
            onDismiss = { showStokKeluarDialog = false }
        )
    }

    if (showHapusDialog) {
        AlertDialog(
            onDismissRequest = { showHapusDialog = false },
            title = { Text("Hapus Obat") },
            text = { Text("Apakah Anda yakin ingin menghapus obat ini?") },
            confirmButton = {
                TextButton(onClick = {
                    obat?.let { viewModel.deleteObat(it) { onBack() } }
                    showHapusDialog = false
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showHapusDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, isWarning: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun RiwayatItem(riwayat: RiwayatStok, formatter: NumberFormat) {
    val dateFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
    val isMasuk = riwayat.jenis == "MASUK"
    val color = if (isMasuk) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isMasuk) "MASUK" else "KELUAR",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Text(
                    text = dateFormat.format(Date(riwayat.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (riwayat.catatan != null) {
                    Text(
                        text = riwayat.catatan!!,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isMasuk) "+" else "-"}${riwayat.qty}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                if (riwayat.harga != null) {
                    Text(
                        text = "Rp ${formatter.format(riwayat.harga)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun StokDialog(
    title: String,
    onConfirm: (qty: Int, harga: Int?, catatan: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var qty by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it.filter { c -> c.isDigit() } },
                    label = { Text("Jumlah") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = harga,
                    onValueChange = { harga = it.filter { c -> c.isDigit() } },
                    label = { Text("Harga per Unit (opsional)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val qtyInt = qty.toIntOrNull()
                if (qtyInt != null && qtyInt > 0) {
                    onConfirm(qtyInt, harga.toIntOrNull(), catatan.ifBlank { null })
                }
            }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
```

- [ ] **Step 5: Wire screens into `NavGraph.kt`**

Replace the TODO stubs in NavGraph:

```kotlin
composable(Routes.OBAT_LIST) {
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
    ObatListScreen(
        viewModel = viewModel,
        onObatClick = { navController.navigate(Routes.obatDetail(it)) },
        onTambahObat = { navController.navigate(Routes.obatForm()) }
    )
}

composable(
    route = Routes.OBAT_FORM,
    arguments = listOf(navArgument("obatId") {
        type = NavType.IntType
        defaultValue = -1
    })
) { backStackEntry ->
    val obatId = backStackEntry.arguments?.getInt("obatId") ?: -1
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
    ObatFormScreen(
        viewModel = viewModel,
        obatId = if (obatId == -1) null else obatId,
        onSaved = { navController.popBackStack() }
    )
}

composable(
    route = Routes.OBAT_DETAIL,
    arguments = listOf(navArgument("obatId") { type = NavType.IntType })
) { backStackEntry ->
    val obatId = backStackEntry.arguments?.getInt("obatId") ?: return@composable
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
    ObatDetailScreen(
        viewModel = viewModel,
        transaksiRepository = app.container.transaksiRepository,
        obatId = obatId,
        onEdit = { navController.navigate(Routes.obatForm(it)) },
        onBack = { navController.popBackStack() }
    )
}
```

Add imports:
```kotlin
import com.example.tes.ui.obat.ObatListScreen
import com.example.tes.ui.obat.ObatFormScreen
import com.example.tes.ui.obat.ObatDetailScreen
import com.example.tes.ui.obat.ObatViewModel
```

---

### Task 11: Transaksi Screens — Transaksi + Riwayat

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/transaksi/TransaksiViewModel.kt`
- Create: `app/src/main/java/com/example/tes/ui/transaksi/TransaksiScreen.kt`
- Create: `app/src/main/java/com/example/tes/ui/transaksi/RiwayatScreen.kt`
- Modify: `app/src/main/java/com/example/tes/ui/navigation/NavGraph.kt` (wire screens)

- [ ] **Step 1: Create `TransaksiViewModel.kt`**

```kotlin
package com.example.tes.ui.transaksi

import androidx.lifecycle.ViewModel
import com.example.tes.data.entity.Obat
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.data.repository.ObatRepository
import com.example.tes.data.repository.TransaksiRepository
import kotlinx.coroutines.flow.Flow

class TransaksiViewModel(
    private val obatRepository: ObatRepository,
    private val transaksiRepository: TransaksiRepository
) : ViewModel() {

    fun searchObat(query: String): Flow<List<Obat>> = obatRepository.searchObat(query)

    fun getAllObat(): Flow<List<Obat>> = obatRepository.getAllObat()

    suspend fun stokMasuk(obatId: Int, qty: Int, harga: Int?, catatan: String?) {
        transaksiRepository.stokMasuk(obatId, qty, harga, catatan)
    }

    suspend fun stokKeluar(obatId: Int, qty: Int, harga: Int?, catatan: String?): Boolean {
        return transaksiRepository.stokKeluar(obatId, qty, harga, catatan)
    }

    fun getRiwayatByObat(obatId: Int): Flow<List<RiwayatStok>> =
        transaksiRepository.getRiwayatByObat(obatId)

    fun getRecentRiwayat(): Flow<List<RiwayatStok>> =
        transaksiRepository.getRecentRiwayat(50)
}
```

- [ ] **Step 2: Create `TransaksiScreen.kt`**

```kotlin
package com.example.tes.ui.transaksi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Obat
import kotlinx.coroutines.launch

@Composable
fun TransaksiScreen(viewModel: TransaksiViewModel) {
    val obatList by viewModel.getAllObat().collectAsState(initial = emptyList())
    var searchText by remember { mutableStateOf("") }
    val filteredObat = if (searchText.isBlank()) obatList
        else viewModel.searchObat(searchText).collectAsState(initial = emptyList()).value

    var selectedObat by remember { mutableStateOf<Obat?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var jenisTransaksi by remember { mutableStateOf("MASUK") }
    var qty by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Transaksi Stok",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari obat...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredObat, key = { it.id }) { obat ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedObat = obat
                            showDialog = true
                            qty = ""
                            harga = ""
                            catatan = ""
                        },
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = obat.nama,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Stok: ${obat.stok}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedObat != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Transaksi: ${selectedObat!!.nama}") },
            text = {
                Column {
                    // Jenis transaksi
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = jenisTransaksi == "MASUK",
                            onClick = { jenisTransaksi = "MASUK" }
                        )
                        Text("Stok Masuk", modifier = Modifier.clickable { jenisTransaksi = "MASUK" })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = jenisTransaksi == "KELUAR",
                            onClick = { jenisTransaksi = "KELUAR" }
                        )
                        Text("Stok Keluar", modifier = Modifier.clickable { jenisTransaksi = "KELUAR" })
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qty,
                        onValueChange = { qty = it.filter { c -> c.isDigit() } },
                        label = { Text("Jumlah") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = harga,
                        onValueChange = { harga = it.filter { c -> c.isDigit() } },
                        label = { Text("Harga per Unit (opsional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = catatan,
                        onValueChange = { catatan = it },
                        label = { Text("Catatan (opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qtyInt = qty.toIntOrNull() ?: return@Button
                    if (qtyInt <= 0) return@Button
                    scope.launch {
                        if (jenisTransaksi == "MASUK") {
                            viewModel.stokMasuk(selectedObat!!.id, qtyInt, harga.toIntOrNull(), catatan.ifBlank { null })
                        } else {
                            val success = viewModel.stokKeluar(selectedObat!!.id, qtyInt, harga.toIntOrNull(), catatan.ifBlank { null })
                            if (!success) {
                                // Stok tidak cukup
                                showDialog = false
                                return@launch
                            }
                        }
                        showDialog = false
                    }
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
```

- [ ] **Step 3: Create `RiwayatScreen.kt`**

```kotlin
package com.example.tes.ui.transaksi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.RiwayatStok
import com.example.tes.ui.obat.RiwayatItem
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(viewModel: TransaksiViewModel) {
    val riwayatList by viewModel.getRecentRiwayat().collectAsState(initial = emptyList())
    val formatter = NumberFormat.getInstance(Locale("id", "ID"))

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Riwayat Transaksi",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(riwayatList, key = { it.id }) { riwayat ->
                RiwayatItem(riwayat = riwayat, formatter = formatter)
            }
        }
    }
}
```

- [ ] **Step 4: Wire screens into `NavGraph.kt`**

```kotlin
composable(Routes.TRANSAKSI) {
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { TransaksiViewModel(app.container.obatRepository, app.container.transaksiRepository) }
    TransaksiScreen(viewModel = viewModel)
}

composable(
    route = Routes.RIWAYAT,
    arguments = listOf(navArgument("obatId") { type = NavType.IntType })
) { backStackEntry ->
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { TransaksiViewModel(app.container.obatRepository, app.container.transaksiRepository) }
    RiwayatScreen(viewModel = viewModel)
}
```

Add imports:
```kotlin
import com.example.tes.ui.transaksi.TransaksiScreen
import com.example.tes.ui.transaksi.RiwayatScreen
import com.example.tes.ui.transaksi.TransaksiViewModel
```

---

### Task 12: ReminderScreen — Obat Hampir Habis & Expired

**Files:**
- Create: `app/src/main/java/com/example/tes/ui/reminder/ReminderViewModel.kt`
- Create: `app/src/main/java/com/example/tes/ui/reminder/ReminderScreen.kt`
- Modify: `app/src/main/java/com/example/tes/ui/navigation/NavGraph.kt` (wire screen)

- [ ] **Step 1: Create `ReminderViewModel.kt`**

```kotlin
package com.example.tes.ui.reminder

import androidx.lifecycle.ViewModel
import com.example.tes.data.entity.Obat
import com.example.tes.data.repository.ObatRepository
import kotlinx.coroutines.flow.Flow

class ReminderViewModel(
    private val obatRepository: ObatRepository
) : ViewModel() {

    fun getStokMenipis(): Flow<List<Obat>> = obatRepository.getStokMenipis()

    fun getAkanExpired(): Flow<List<Obat>> = obatRepository.getAkanExpired()
}
```

- [ ] **Step 2: Create `ReminderScreen.kt`**

```kotlin
package com.example.tes.ui.reminder

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Obat

@Composable
fun ReminderScreen(
    viewModel: ReminderViewModel,
    onObatClick: (Int) -> Unit
) {
    val stokMenipis by viewModel.getStokMenipis().collectAsState(initial = emptyList())
    val akanExpired by viewModel.getAkanExpired().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Pengingat",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section: Stok Menipis
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Stok Hampir Habis (${stokMenipis.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (stokMenipis.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada obat dengan stok menipis",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
            }
        } else {
            items(stokMenipis, key = { "sm_${it.id}" }) { obat ->
                ReminderItem(
                    obat = obat,
                    type = "STOK",
                    onClick = { onObatClick(obat.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Akan Expired (${akanExpired.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (akanExpired.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada obat yang akan expired",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp)
                )
            }
        } else {
            items(akanExpired, key = { "exp_${it.id}" }) { obat ->
                ReminderItem(
                    obat = obat,
                    type = "EXPIRED",
                    onClick = { onObatClick(obat.id) }
                )
            }
        }
    }
}

@Composable
fun ReminderItem(obat: Obat, type: String, onClick: () -> Unit) {
    val color = if (type == "STOK") MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.tertiary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = obat.nama,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (type == "STOK") "Stok: ${obat.stok} (Min: ${obat.stokMinimum})"
                    else "Expired: ${obat.expiredDate ?: "-"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (type == "STOK") "${obat.stok}" else obat.expiredDate ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
```

- [ ] **Step 3: Wire into `NavGraph.kt`**

```kotlin
composable(Routes.REMINDER) {
    val app = LocalContext.current.applicationContext as TesApplication
    val viewModel = remember { ReminderViewModel(app.container.obatRepository) }
    ReminderScreen(
        viewModel = viewModel,
        onObatClick = { navController.navigate(Routes.obatDetail(it)) }
    )
}
```

Add imports:
```kotlin
import com.example.tes.ui.reminder.ReminderScreen
import com.example.tes.ui.reminder.ReminderViewModel
```

---

### Task 13: Clean Build & Verification

**Files:** (none needed)

- [ ] **Step 1: Run clean build**

Run: `./gradlew clean assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Verify all navigation works**

Run on emulator, verify:
- App launches to Home tab with dashboard (0 count for everything)
- Bottom nav switches between 4 tabs
- Obat tab: can add new medicine via FAB
- Obat form: all fields work, saves correctly
- Obat list: shows added medicine, search works
- Transaksi tab: can search & select medicine, stok masuk/keluar works
- Detail screen: shows medicine info, stok actions, riwayat
- Reminder tab: shows stok menipis / expired items
