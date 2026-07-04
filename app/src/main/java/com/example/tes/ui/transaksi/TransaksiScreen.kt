package com.example.tes.ui.transaksi

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    val obatList by viewModel.allObat.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val filteredObat = if (searchText.isBlank()) {
        obatList
    } else {
        viewModel.searchObat(searchText).collectAsState(initial = emptyList()).value
    }

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
                            viewModel.stokKeluar(selectedObat!!.id, qtyInt, harga.toIntOrNull(), catatan.ifBlank { null })
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
