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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
                    o.expiredDate?.let { InfoRow("Expired", it) }
                    o.deskripsi?.let { InfoRow("Deskripsi", it) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                RiwayatItem(riwayat = r, formatter = formatter)
            }
        }
    }

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
                        // Stok tidak cukup
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
                riwayat.catatan?.let { cat ->
                    Text(
                        text = cat,
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
