package com.example.scrapbooking.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.scrapbooking.domain.model.Stamp
import com.example.scrapbooking.domain.repository.StampRepository
import kotlinx.coroutines.launch

@HiltViewModel
class AlbumViewModel @Inject constructor(
    private val stampRepository: StampRepository
) : ViewModel() {
    private val _stampImages = MutableStateFlow<List<Stamp>>(emptyList())
    val stampImages: StateFlow<List<Stamp>> = _stampImages.asStateFlow()

    init {
        viewModelScope.launch {
            stampRepository.getStampImages().collect { stamps ->
                _stampImages.value = stamps
            }
        }
    }
}
