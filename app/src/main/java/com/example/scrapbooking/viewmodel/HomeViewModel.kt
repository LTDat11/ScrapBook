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
                    var savedBitmap: android.graphics.Bitmap? = null

                    // Helper: center-crop to target aspect ratio (preserve resolution)
                    fun centerCropToAspect(src: android.graphics.Bitmap, aspect: Float): android.graphics.Bitmap {
                        val w = src.width
                        val h = src.height
                        val srcAspect = w.toFloat() / h.toFloat()
                        return if (srcAspect > aspect) {
                            // source is wider -> crop left/right
                            val newW = (h * aspect).toInt()
                            val left = (w - newW) / 2
                            android.graphics.Bitmap.createBitmap(src, left, 0, newW, h)
                        } else if (srcAspect < aspect) {
                            // source is taller -> crop top/bottom
                            val newH = (w / aspect).toInt()
                            val top = (h - newH) / 2
                            android.graphics.Bitmap.createBitmap(src, 0, top, w, newH)
                        } else src
                    }

                    withContext(Dispatchers.IO) {
                        file.parentFile?.mkdirs()

                        // Match preview aspect in StampPopup (155dp x 190dp)
                        val previewAspect = 155f / 190f
                        val croppedForPreview = centerCropToAspect(bitmap, previewAspect)

                        val density = context.resources.displayMetrics.density
                        val perforationRadiusPx = 5.5f * density // same default as StampShape

                        val finalBitmap = try {
                            cameraRepository.applyStampMask(
                                croppedForPreview,
                                hPerforations = 7,
                                vPerforations = 9,
                                perforationRadiusPx = perforationRadiusPx
                            )
                        } catch (e: Exception) {
                            croppedForPreview
                        }

                        java.io.FileOutputStream(file).use { out ->
                            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                        }
                        savedBitmap = finalBitmap
                    }

                    // Pass metadata to repository so Stamp carries date/time/location
                    val result = stampRepository.saveStampImage(
                        savedBitmap ?: bitmap,
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