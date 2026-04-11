package com.example.scrapbooking.data.repository

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Color
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

    /**
     * Draw an overlay bitmap (mask) over the source bitmap.
     * If overlay size differs, it will be scaled to match source.
     * Returns a new ARGB_8888 bitmap.
     */
    fun applyOverlayMask(src: Bitmap, overlay: Bitmap): Bitmap {
        val overlayBitmap = if (overlay.width != src.width || overlay.height != src.height) {
            Bitmap.createScaledBitmap(overlay, src.width, src.height, true)
        } else overlay

        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(overlayBitmap, 0f, 0f, paint)
        return result
    }

    /**
     * Apply stamp-shaped mask (perforated edges) to the given bitmap.
     * The resulting bitmap keeps the original pixels inside the stamp path
     * and makes the outside transparent (ARGB_8888).
     * @param hPerforations number of semicircles on horizontal edges
     * @param vPerforations number of semicircles on vertical edges
     * @param perforationRadiusPx radius in pixels for each semicircle
     */
    fun applyStampMask(
        src: Bitmap,
        hPerforations: Int = 7,
        vPerforations: Int = 9,
        perforationRadiusPx: Float
    ): Bitmap {
        val w = src.width
        val h = src.height

        // Build android.graphics.Path replicating StampShape.buildStampPath
        val path = Path()

        val hStep = w / hPerforations.toFloat()
        val vStep = h / vPerforations.toFloat()
        val r = perforationRadiusPx

        path.moveTo(0f, 0f)
        for (i in 0 until hPerforations) {
            val cx = hStep * i + hStep / 2f
            val rect = RectF(cx - r, -r, cx + r, r)
            path.arcTo(rect, 180f, -180f, false)
        }
        path.lineTo(w.toFloat(), 0f)

        for (i in 0 until vPerforations) {
            val cy = vStep * i + vStep / 2f
            val rect = RectF(w - r, cy - r, w + r, cy + r)
            path.arcTo(rect, 270f, -180f, false)
        }
        path.lineTo(w.toFloat(), h.toFloat())

        for (i in hPerforations - 1 downTo 0) {
            val cx = hStep * i + hStep / 2f
            val rect = RectF(cx - r, h - r, cx + r, h + r)
            path.arcTo(rect, 0f, -180f, false)
        }
        path.lineTo(0f, h.toFloat())

        for (i in vPerforations - 1 downTo 0) {
            val cy = vStep * i + vStep / 2f
            val rect = RectF(-r, cy - r, r, cy + r)
            path.arcTo(rect, 90f, -180f, false)
        }
        path.close()

        // Create mask bitmap where path area = opaque, outside = transparent
        val mask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val maskCanvas = Canvas(mask)
        val maskPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        maskCanvas.drawPath(path, maskPaint)

        // Apply mask using PorterDuff DST_IN
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(src, 0f, 0f, null)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        canvas.drawBitmap(mask, 0f, 0f, paint)
        paint.xfermode = null

        return result
    }
}
