package com.example.scrapbooking.ui.navigation

sealed class Screen(val route: String) {
    object Home: Screen("home")
    object Gallery: Screen("gallery")
    object Detail: Screen("detail")
}