package com.example.scrapbooking.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor() : ViewModel() {
    private val _photos = MutableStateFlow<Map<LocalDate, List<String>>>(emptyMap())
    val photos: StateFlow<Map<LocalDate, List<String>>> = _photos.asStateFlow()

    init {
        loadMockData()
    }

    private fun loadMockData() {
        val today = LocalDate.now()
        _photos.value = mapOf(
            today to listOf("https://picsum.photos/seed/1/200/200", "https://picsum.photos/seed/2/200/200"),
            today.minusDays(2) to listOf("https://picsum.photos/seed/3/200/200"),
            today.minusDays(5) to listOf("https://picsum.photos/seed/4/200/200", "https://picsum.photos/seed/5/200/200", "https://picsum.photos/seed/6/200/200"),
            today.minusDays(10) to listOf("https://picsum.photos/seed/7/200/200")
        )
    }
}
