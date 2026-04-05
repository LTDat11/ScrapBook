package com.example.scrapbooking.ui.state

import android.graphics.Bitmap

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Ready : HomeUiState()
    object PermissionDenied : HomeUiState()
    object Capturing : HomeUiState()
    data class CapturedStamp(val bitmap: Bitmap) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}