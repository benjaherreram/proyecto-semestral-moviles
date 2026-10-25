package com.example.cappucare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cappucare.ui.*
import com.example.cappucare.viewmodel.InventarioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { Surface { App() } }
        }
    }
}

@Composable
fun App() {
    val nav = rememberNavController()
    val vm: InventarioViewModel = viewModel() // mismo vm para todas las pantallas

    NavHost(
        navController = nav, startDestination = "lista",
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable("lista") {
            ListaScreen(vm, onNuevo = { nav.navigate("form") }, onMov = { id -> nav.navigate("mov/$id") })
        }
        composable("form") {
            FormularioScreen(vm, onVolver = { nav.popBackStack() })
        }
        composable("mov/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
            val id = it.arguments!!.getInt("id")
            MovimientoScreen(vm, id, onVolver = { nav.popBackStack() })
        }
    }
}