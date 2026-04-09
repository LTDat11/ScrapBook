package com.example.scrapbooking.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.scrapbooking.domain.model.Stamp
import com.example.scrapbooking.domain.repository.StampRepository
import com.example.scrapbooking.ui.state.AlbumUiState
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val stampRepository: StampRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<AlbumUiState>(AlbumUiState.Loading)
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            stampRepository.getStampImages()
                .catch { e ->
                    _uiState.value = AlbumUiState.Error(e.message ?: "Unknown error")
                }
                .collect { stamps ->
                    val photosByDate = stamps
                        .groupBy {
                            java.time.Instant.ofEpochMilli(it.lastModified)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        .mapValues { entry ->
                            entry.value.sortedBy { it.lastModified }
                        }
                    _uiState.value = AlbumUiState.Success(photosByDate)
                }
        }
    }
}
