package com.example.tes.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tes.data.entity.KategoriStok

private val chartColors = listOf(
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFFFF9800),
    Color(0xFF9C27B0),
    Color(0xFF00BCD4)
)

@Composable
fun BarChart(
    data: List<KategoriStok>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty() || data.all { it.totalStok == 0 }) return

    val maxStok = data.maxOf { it.totalStok }
    if (maxStok == 0) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Stok per Kategori",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        data.forEachIndexed { index, item ->
            val fraction = item.totalStok.toFloat() / maxStok
            val barColor = chartColors[index % chartColors.size]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.namaKategori,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(90.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth()) {
                        val barWidth = size.width * fraction.coerceIn(0.05f, 1f)
                        drawRoundRect(
                            color = barColor.copy(alpha = 0.8f),
                            size = Size(barWidth, size.height),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.totalStok.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
}
