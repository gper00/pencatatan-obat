package com.example.tes.ui.transaksi

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.ui.obat.RiwayatItem
import java.text.NumberFormat
import java.util.Locale

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
