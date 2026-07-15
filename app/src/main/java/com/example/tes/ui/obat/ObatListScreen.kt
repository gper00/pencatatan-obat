package com.example.tes.ui.obat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.Obat
import kotlinx.coroutines.delay

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

    val context = LocalContext.current
    val exportMessage by viewModel.exportMessage.collectAsState()
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportMessage) {
        if (exportMessage != null) {
            delay(3000)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onTambahObat) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Obat")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Obat",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

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

            if (exportMessage != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(exportMessage!!)
                }
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Inventaris") },
            text = { Text("Pilih format laporan:") },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.exportInventaris(context, "PDF")
                        showExportDialog = false
                    }) { Text("📄 PDF") }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = {
                        viewModel.exportInventaris(context, "Excel")
                        showExportDialog = false
                    }) { Text("📊 Excel") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Batal") }
            }
        )
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
