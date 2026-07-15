package com.example.tes.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tes.TesApplication
import com.example.tes.ui.home.HomeScreen
import com.example.tes.ui.home.HomeViewModel
import com.example.tes.ui.obat.ObatDetailScreen
import com.example.tes.ui.obat.ObatFormScreen
import com.example.tes.ui.obat.ObatListScreen
import com.example.tes.ui.obat.ObatViewModel
import com.example.tes.ui.reminder.ReminderScreen
import com.example.tes.ui.reminder.ReminderViewModel
import com.example.tes.ui.transaksi.GlobalRiwayatScreen
import com.example.tes.ui.transaksi.RiwayatViewModel
import com.example.tes.ui.transaksi.TransaksiScreen
import com.example.tes.ui.transaksi.TransaksiViewModel

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember {
                HomeViewModel(
                    app.container.obatRepository,
                    app.container.transaksiRepository,
                    app.container.dataSeeder
                )
            }
            HomeScreen(viewModel = viewModel)
        }

        composable(Routes.OBAT_LIST) {
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
            ObatListScreen(
                viewModel = viewModel,
                onObatClick = { navController.navigate(Routes.obatDetail(it)) },
                onTambahObat = { navController.navigate(Routes.obatForm()) }
            )
        }

        composable(
            route = Routes.OBAT_FORM,
            arguments = listOf(navArgument("obatId") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val obatId = backStackEntry.arguments?.getInt("obatId") ?: -1
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
            ObatFormScreen(
                viewModel = viewModel,
                obatId = if (obatId == -1) null else obatId,
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.OBAT_DETAIL,
            arguments = listOf(navArgument("obatId") { type = NavType.IntType })
        ) { backStackEntry ->
            val obatId = backStackEntry.arguments?.getInt("obatId") ?: return@composable
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember { ObatViewModel(app.container.obatRepository, app.container.transaksiRepository) }
            ObatDetailScreen(
                viewModel = viewModel,
                transaksiRepository = app.container.transaksiRepository,
                obatId = obatId,
                onEdit = { navController.navigate(Routes.obatForm(it)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TRANSAKSI) {
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember { TransaksiViewModel(app.container.obatRepository, app.container.transaksiRepository) }
            TransaksiScreen(
                viewModel = viewModel,
                onRiwayatClick = { navController.navigate(Routes.RIWAYAT_GLOBAL) }
            )
        }

        composable(Routes.RIWAYAT_GLOBAL) {
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember {
                RiwayatViewModel(app.container.transaksiRepository, app.container.obatRepository)
            }
            GlobalRiwayatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REMINDER) {
            val app = LocalContext.current.applicationContext as TesApplication
            val viewModel = remember { ReminderViewModel(app.container.obatRepository) }
            ReminderScreen(
                viewModel = viewModel,
                onObatClick = { navController.navigate(Routes.obatDetail(it)) }
            )
        }
    }
}
