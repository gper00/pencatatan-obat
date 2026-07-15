package com.example.tes.ui.reminder

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun ReminderScreen(
    viewModel: ReminderViewModel,
    onObatClick: (Int) -> Unit
) {
    val stokMenipis by viewModel.getStokMenipis().collectAsState(initial = emptyList())
    val akanExpired by viewModel.getAkanExpired().collectAsState(initial = emptyList())
    val exportMessage by viewModel.exportMessage.collectAsState()
    val context = LocalContext.current
    var showExportDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportMessage) {
        if (exportMessage != null) {
            delay(3000)
            viewModel.clearExportMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pengingat",
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
                Spacer(modifier = Modifier.height(16.dp))
            }

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

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Laporan Stok Kritis") },
            text = { Text("Pilih format laporan:") },
            confirmButton = {
                Row {
                    TextButton(onClick = {
                        viewModel.exportStokKritis(context, "PDF")
                        showExportDialog = false
                    }) { Text("📄 PDF") }
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(onClick = {
                        viewModel.exportStokKritis(context, "Excel")
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
