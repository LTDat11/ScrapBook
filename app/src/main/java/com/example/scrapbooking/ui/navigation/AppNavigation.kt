package com.example.scrapbooking.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.time.LocalDate
import java.time.format.DateTimeParseException
import com.example.scrapbooking.ui.components.BottomNavigationBar
import com.example.scrapbooking.ui.screens.AlbumScreen
import com.example.scrapbooking.ui.screens.AlbumDetailScreen
import com.example.scrapbooking.ui.screens.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        // Nội dung chính (Home/Gallery) chiếm toàn bộ màn hình
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Gallery.route) {
                AlbumScreen(onDayClick = { date ->
                    navController.navigate(Screen.Detail.createRoute(date.toString()))
                })
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument(Screen.Detail.ARG_DATE) { type = NavType.StringType })
            ) { backStackEntry ->
                val dateStr = backStackEntry.arguments?.getString(Screen.Detail.ARG_DATE)
                val date = try {
                    if (dateStr != null) LocalDate.parse(dateStr) else LocalDate.now()
                } catch (e: DateTimeParseException) {
                    LocalDate.now()
                }
                AlbumDetailScreen(date = date, onBack = { navController.popBackStack() })
            }
        }
        // Bottom navigation bar nằm đè lên trên, canh dưới cùng
        BottomNavigationBar(
            navController = navController,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}