package com.example.scrapbooking.ui.state

import com.example.scrapbooking.domain.model.Stamp
import java.time.LocalDate

sealed interface AlbumUiState {
    object Loading : AlbumUiState
    data class Success(
        val photosByDate: Map<LocalDate, List<Stamp>>
    ) : AlbumUiState
    data class Error(val message: String) : AlbumUiState
}