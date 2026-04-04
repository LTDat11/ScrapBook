package com.example.scrapbooking.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrapbooking.ui.screens.CameraScreen
import com.example.scrapbooking.ui.screens.GalleryScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "camera_screen"
    ) {
        composable("camera_screen") {
            // Screen 1: Chụp ảnh
            CameraScreen(
                onNavigateToGallery = { navController.navigate("gallery_screen") }
            )
        }
        composable("gallery_screen") {
            // Screen 2: Xem bộ sưu tập tem
            GalleryScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}