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
