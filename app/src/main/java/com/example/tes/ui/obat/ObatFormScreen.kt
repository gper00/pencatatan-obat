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
import androidx.compose.runtime.LaunchedEffect
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

    LaunchedEffect(obatId) {
        if (obatId != null) {
            viewModel.loadObat(obatId)
        }
    }

    LaunchedEffect(selectedObat) {
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
