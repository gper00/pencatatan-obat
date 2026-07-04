# Pencatatan Obat — Aplikasi Android

> **Tanggal**: 2026-07-04
> **Status**: Draft — menunggu review
> **Stack**: Kotlin + Jetpack Compose + Room + MVVM

## 1. Ringkasan

Aplikasi pencatatan stok obat berbasis Android native untuk pengguna tunggal (kerabat penjual obat). Fokus pada pencatatan stok, transaksi masuk/keluar, riwayat perubahan, dan notifikasi obat akan kedaluwarsa atau stok menipis.

## 2. Target Pengguna

- Satu orang pengguna (kerabat pengusul)
- Skala usaha: toko obat kecil (bukan apotek/distributor besar)

## 3. Stack Teknologi

| Komponen | Pilihan |
|---|---|
| Bahasa | Kotlin |
| UI Toolkit | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Arsitektur | MVVM (UI → ViewModel → Repository → Room DAO) |
| Dependency Injection | Manual DI (AppContainer) |
| Navigation | Navigation Compose + Bottom Navigation |
| Background | WorkManager (untuk reminder/notifikasi) |

## 4. Struktur Project

```
app/src/main/java/com/example/tes/
├── MainActivity.kt
├── TesApplication.kt
├── di/
│   └── AppContainer.kt              # Manual DI container
├── data/
│   ├── entity/
│   │   ├── Obat.kt
│   │   ├── Kategori.kt
│   │   ├── Satuan.kt
│   │   └── RiwayatStok.kt
│   ├── dao/
│   │   ├── ObatDao.kt
│   │   ├── KategoriDao.kt
│   │   ├── SatuanDao.kt
│   │   └── RiwayatStokDao.kt
│   ├── database/
│   │   └── AppDatabase.kt
│   └── repository/
│       ├── ObatRepository.kt
│       └── TransaksiRepository.kt
├── ui/
│   ├── navigation/
│   │   ├── BottomNavItem.kt
│   │   └── NavGraph.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── components/
│   │   └── (shared composables)
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── obat/
│   │   ├── ObatListScreen.kt
│   │   ├── ObatDetailScreen.kt
│   │   ├── ObatFormScreen.kt
│   │   └── ObatViewModel.kt
│   ├── transaksi/
│   │   ├── TransaksiScreen.kt
│   │   ├── RiwayatScreen.kt
│   │   └── TransaksiViewModel.kt
│   └── reminder/
│       ├── ReminderScreen.kt
│       └── ReminderViewModel.kt
```

## 5. Database (ERD & Tabel)

### Relationship

```
kategori (1) ───< obat >─── (1) satuan
                 │
                 │
                 ▼
           riwayat_stok
```

### Tabel: kategori

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | Auto-increment |
| nama | TEXT (UNIQUE) | Nama kategori |

### Tabel: satuan

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | Auto-increment |
| nama | TEXT (UNIQUE) | Nama satuan |

### Tabel: obat

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | Auto-increment |
| kode | TEXT (UNIQUE) | Kode unik obat |
| nama | TEXT | Nama obat |
| kategori_id | INTEGER (FK → kategori.id) | Kategori obat |
| satuan_id | INTEGER (FK → satuan.id) | Satuan obat |
| stok | INTEGER | Stok saat ini |
| stok_minimum | INTEGER | Ambang batas minimum |
| harga_beli | INTEGER | Harga beli (dalam rupiah) |
| harga_jual | INTEGER | Harga jual (dalam rupiah) |
| expired_date | TEXT (nullable) | Tanggal kedaluwarsa (ISO 8601) |
| deskripsi | TEXT (nullable) | Catatan tambahan |
| created_at | DATETIME | Waktu dibuat |
| updated_at | DATETIME | Waktu diupdate |

### Tabel: riwayat_stok

| Kolom | Tipe | Keterangan |
|---|---|---|
| id | INTEGER (PK) | Auto-increment |
| obat_id | INTEGER (FK → obat.id, CASCADE) | Obat terkait |
| jenis | TEXT (CHECK: MASUK/KELUAR/PENYESUAIAN) | Jenis transaksi |
| qty | INTEGER (>0) | Jumlah |
| harga | INTEGER (nullable) | Harga per unit saat transaksi |
| catatan | TEXT (nullable) | Keterangan |
| created_at | DATETIME | Waktu transaksi |

### Index

- `idx_obat_nama` ON `obat(nama)`
- `idx_obat_kategori` ON `obat(kategori_id)`
- `idx_riwayat_obat` ON `riwayat_stok(obat_id)`
- `idx_expired` ON `obat(expired_date)`

## 6. Arsitektur & Alur Data

### MVVM

```
[Composable Screen] ──(State)── [ViewModel] ──(suspend)── [Repository] ──(DAO)── [Room DB]
        │                              │
   observes StateFlow            exposes StateFlow
        │                              │
   events (klik, input) ──────────> calls suspend fun
```

### Aturan Bisnis

1. **Transaksi stok**: Setiap tambah/kurang stok → insert ke `riwayat_stok` → update `obat.stok` secara atomik dalam 1 transaction Room.
2. **Stok tidak boleh negatif**: Validasi sebelum stok keluar.
3. **Satu user, tanpa login**: Aplikasi langsung terbuka ke Home.

## 7. Navigasi & Halaman

### Bottom Navigation — 4 Tab

| Tab | Ikon | Screen | ViewModel |
|---|---|---|---|
| 🏠 Home | `Icons.Default.Home` | HomeScreen | HomeViewModel |
| 💊 Obat | `Icons.Default.Medication` | ObatListScreen | ObatViewModel |
| 📦 Transaksi | `Icons.Default.SwapHoriz` | TransaksiScreen | TransaksiViewModel |
| ⏰ Reminder | `Icons.Default.Notifications` | ReminderScreen | ReminderViewModel |

### Flow Navigasi

```
HomeScreen (BottomNav)
├── ObatListScreen
│   ├── → ObatDetailScreen (klik item)
│   │   ├── → ObatFormScreen (edit)
│   │   ├── → TambahStokDialog
│   │   └── → KurangiStokDialog
│   └── → ObatFormScreen (FAB tambah)
├── TransaksiScreen
│   ├── → PilihObatDialog → input qty
│   └── → RiwayatScreen (lihat semua riwayat)
└── ReminderScreen
    └── → ObatDetailScreen (klik item)
```

## 8. Layer UI — Detail Layar

### 8.1 HomeScreen (Dashboard)
- **KPI Cards**: Total obat, hampir habis (stok ≤ stok_minimum), akan expired (≤ 30 hari)
- **List**: 5 obat terakhir ditambahkan atau stok menipis
- **FAB**: Navigasi ke Tambah Obat

### 8.2 ObatListScreen
- **Search bar**: Filter berdasarkan nama
- **LazyColumn**: Card per obat (nama, stok, expired badge)
- **Swipe to delete** atau konfirmasi hapus
- **FAB**: Tambah obat baru

### 8.3 ObatDetailScreen
- Informasi lengkap: kode, nama, kategori, satuan, stok, harga, expired
- Tombol aksi: Tambah Stok, Kurangi Stok, Edit, Hapus
- Riwayat list 10 transaksi terakhir (link ke halaman riwayat penuh)

### 8.4 ObatFormScreen (Tambah & Edit)
- Input: Nama, Kategori (dropdown), Satuan (dropdown), Stok Awal, Stok Minimum, Harga Beli, Harga Jual, Expired Date (datepicker)
- Mode Edit: field stok tidak bisa diubah (harus melalui transaksi)

### 8.5 TransaksiScreen
- Pilih obat (search + list)
- Pilih jenis: Masuk / Keluar
- Input qty dan catatan (opsional)
- Validasi stok cukup untuk keluar

### 8.6 RiwayatScreen
- LazyColumn semua riwayat, filter per obat
- Warna: hijau untuk MASUK, merah untuk KELUAR

### 8.7 ReminderScreen
- 2 section: **Hampir Habis** (stok ≤ minimum) dan **Akan Expired** (dalam 30 hari)
- List item → tap ke DetailObat

## 9. Notifikasi (Future Enhancement)

- WorkManager untuk periodic check setiap 24 jam
- Notifikasi channel: "Obat Hampir Habis", "Obat Expired"
- Tidak akan diimplementasikan di fase pertama

## 10. Milestone Implementasi

### Fase 1 — Core (CRUD + Transaksi)
1. Setup project: Compose, Room, Navigation (dari template existing)
2. Entity + DAO + Database
3. Manual DI (AppContainer)
4. Repository
5. HomeScreen + HomeViewModel
6. ObatListScreen + ObatFormScreen + ObatViewModel
7. TransaksiScreen + RiwayatScreen + TransaksiViewModel
8. ReminderScreen + ReminderViewModel

### Fase 2 (Post-MVP)
- WorkManager notifikasi
- Export laporan (PDF/CSV)
- Backup database
- Foto obat

## 11. Error & Edge Cases

| Kasus | Penanganan |
|---|---|
| Stok minus | Validasi di ViewModel, toast error |
| Nama obat kosong | Form tidak bisa submit |
| Kategori/satuan belum ada | Pre-seed data default saat DB dibuat |
| Expired date sudah lewat | Tampilkan badge merah |
| Harga = 0 | Tampilkan "—" |
| Input qty non-numerik | Keyboard numerik, filter input |
