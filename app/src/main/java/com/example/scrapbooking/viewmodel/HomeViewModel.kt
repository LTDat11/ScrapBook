package com.example.scrapbooking.viewmodel

import androidx.camera.view.PreviewView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scrapbooking.data.repository.CameraRepository
import com.example.scrapbooking.ui.state.HomeUiState
import android.content.Context
import com.example.scrapbooking.R
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
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

    fun setError(message: String) {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Error(message)
        }
    }

    fun captureStamp(previewView: PreviewView) {
        if (_uiState.value is HomeUiState.Capturing) return
        viewModelScope.launch {
            _uiState.value = HomeUiState.Capturing
            val raw = cameraRepository.captureRaw(previewView)
            if (raw == null) {
                _uiState.value = HomeUiState.Error(context.getString(R.string.error_cannot_capture))
                return@launch
            }
            // Crop nặng hơn → đẩy sang IO thread
            val cropped = withContext(Dispatchers.IO) {
                cameraRepository.cropToMask(raw)
            }
            _uiState.value = HomeUiState.CapturedStamp(cropped)
        }
    }

    fun dismissStamp() {
        _uiState.value = HomeUiState.Ready
    }

    fun saveStamp() {
        // Thực hiện lưu ảnh (vào db/storage) ở đây
        // Sau đó trở về trạng thái Ready
        dismissStamp()
    }
}