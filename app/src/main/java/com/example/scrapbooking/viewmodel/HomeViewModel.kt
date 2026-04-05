package com.example.scrapbooking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scrapbooking.ui.state.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel(){
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState : StateFlow<HomeUiState> = _uiState

    fun setCameraReady(){
        viewModelScope.launch {
            _uiState.value = HomeUiState.Ready
        }
    }

    fun setPermissionDenied(){
        viewModelScope.launch {
            _uiState.value = HomeUiState.PermissionDenied
        }
    }

    fun setError(message: String){
        viewModelScope.launch {
            _uiState.value = HomeUiState.Error(message)
        }
    }
}