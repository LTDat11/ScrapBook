package com.example.scrapbooking.data.repository

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tỉ lệ vùng "cửa sổ" bên trong khung mask2
 * (ước lượng từ ảnh: frame kim loại chiếm ~17% ngang và ~20% dọc)
 */
data class MaskRatios(
    val leftRatio: Float   = 0.32f,
    val rightRatio: Float  = 0.70f,
    val topRatio: Float    = 0.37f,
    val bottomRatio: Float = 0.65f
)

@Singleton
class CameraRepository @Inject constructor() {

    /**
     * Chụp bitmap RAW từ PreviewView.
     * ⚠️ Phải gọi trên MAIN thread (yêu cầu của PreviewView.getBitmap()).
     */
    fun captureRaw(previewView: PreviewView): Bitmap? = previewView.bitmap

    /**
     * Crop bitmap theo vùng mask. An toàn gọi trên bất kỳ thread nào.
     */
    fun cropToMask(src: Bitmap, mask: MaskRatios = MaskRatios()): Bitmap {
        val x = (src.width  * mask.leftRatio).toInt().coerceAtLeast(0)
        val y = (src.height * mask.topRatio).toInt().coerceAtLeast(0)
        val w = (src.width  * (mask.rightRatio  - mask.leftRatio)).toInt()
            .coerceAtMost(src.width  - x).coerceAtLeast(1)
        val h = (src.height * (mask.bottomRatio - mask.topRatio)).toInt()
            .coerceAtMost(src.height - y).coerceAtLeast(1)
        return Bitmap.createBitmap(src, x, y, w, h)
    }
}
