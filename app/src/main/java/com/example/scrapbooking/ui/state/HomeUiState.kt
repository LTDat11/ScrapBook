package com.example.scrapbooking.ui.state

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Ready : HomeUiState()
    object PermissionDenied : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}