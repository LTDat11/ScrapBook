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
import com.example.scrapbooking.domain.repository.StampRepository
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val cameraRepository: CameraRepository,
    private val stampRepository: StampRepository,
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

            // Tạo metadata (date/time). Location chưa tích hợp, để null.
            val now = Date()
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = dateFormatter.format(now)
            val time = timeFormatter.format(now)
            val location: String? = null

            _uiState.value = HomeUiState.CapturedStamp(cropped, date = date, time = time, location = location)
        }
    }

    fun dismissStamp() {
        _uiState.value = HomeUiState.Ready
    }

    fun saveStamp() {
        val state = _uiState.value
        if (state is HomeUiState.CapturedStamp) {
            viewModelScope.launch {
                val bitmap = state.bitmap
                val fileName = "stamp_${UUID.randomUUID()}.png"
                val file = java.io.File(context.filesDir, "stamps/$fileName")
                try {
                    withContext(Dispatchers.IO) {
                        file.parentFile?.mkdirs()
                        java.io.FileOutputStream(file).use { out ->
                            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                        // Pass metadata to repository so Stamp carries date/time/location
                        val result = stampRepository.saveStampImage(
                            bitmap,
                            file.absolutePath,
                            date = state.date,
                            time = state.time,
                            location = state.location
                        )
                    if (result.isSuccess) {
                        _uiState.value = HomeUiState.Ready
                    } else {
                        _uiState.value = HomeUiState.Error(context.getString(R.string.error_cannot_capture))
                    }
                } catch (e: Exception) {
                    _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
                }
            }
        } else {
            dismissStamp()
        }
    }
}