package com.example.tes.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tes.data.entity.TrenHari
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val warnaMasuk = Color(0xFF4CAF50)
private val warnaKeluar = Color(0xFFE53935)

private val hariIndo = listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab")

@Composable
fun LineChart(
    data: List<TrenHari>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    if (data.all { it.masuk == 0 && it.keluar == 0 }) {
        Text(
            text = "Belum ada transaksi minggu ini",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = 8.dp)
        )
        return
    }

    val maxVal = data.maxOf { maxOf(it.masuk, it.keluar) }.coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Tren 7 Hari Terakhir",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendDot(color = warnaMasuk, label = "Masuk")
            LegendDot(color = warnaKeluar, label = "Keluar")
        }
        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val stepX = if (data.size <= 1) size.width / 2f else size.width / (data.size - 1)
            val chartHeight = size.height - 8f

            fun titikY(value: Int): Float {
                return chartHeight - (value.toFloat() / maxVal * (chartHeight - 20f)) + 4f
            }

            // Garis MASUK (hijau)
            if (data.any { it.masuk > 0 }) {
                val pathMasuk = Path()
                data.forEachIndexed { i, item ->
                    val x = i * stepX
                    val y = titikY(item.masuk)
                    if (i == 0) pathMasuk.moveTo(x, y)
                    else pathMasuk.lineTo(x, y)
                }
                drawPath(
                    pathMasuk,
                    color = warnaMasuk,
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                data.forEachIndexed { i, item ->
                    val x = i * stepX
                    val y = titikY(item.masuk)
                    drawCircle(color = warnaMasuk, radius = 3f, center = Offset(x, y))
                }
            }

            // Garis KELUAR (merah)
            if (data.any { it.keluar > 0 }) {
                val pathKeluar = Path()
                data.forEachIndexed { i, item ->
                    val x = i * stepX
                    val y = titikY(item.keluar)
                    if (i == 0) pathKeluar.moveTo(x, y)
                    else pathKeluar.lineTo(x, y)
                }
                drawPath(
                    pathKeluar,
                    color = warnaKeluar,
                    style = Stroke(
                        width = 2.5f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
                data.forEachIndexed { i, item ->
                    val x = i * stepX
                    val y = titikY(item.keluar)
                    drawCircle(color = warnaKeluar, radius = 3f, center = Offset(x, y))
                }
            }
        }

        // Label sumbu X (hari)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { item ->
                val dayLabel = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(item.tanggal)
                    val cal = Calendar.getInstance()
                    cal.time = date!!
                    hariIndo[cal.get(Calendar.DAY_OF_WEEK) - 1]
                } catch (e: Exception) {
                    item.tanggal.takeLast(5)
                }
                Text(
                    text = dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
