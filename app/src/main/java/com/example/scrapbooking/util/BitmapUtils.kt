package com.example.scrapbooking.util

import android.content.Context
import android.graphics.*
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat
import com.example.scrapbooking.R
import androidx.core.graphics.createBitmap

object BitmapUtils {

    fun createStampBitmapFromImageProxy(context: Context, image: ImageProxy): Bitmap {
        // 1. Fix xoay ảnh và lấy Bitmap gốc
        val originalBitmap = imageProxyToCorrectBitmap(image)
        image.close()

        // 2. Lấy Mask để làm chuẩn kích thước
        val maskDrawable = ContextCompat.getDrawable(context, R.drawable.mask)
        val maskWidth = maskDrawable!!.intrinsicWidth
        val maskHeight = maskDrawable.intrinsicHeight

        // Tạo kết quả cuối cùng
        val result = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // --- BƯỚC 3: TÍNH TOÁN VÙNG HIỂN THỊ ẢNH (Phần ruột con tem) ---
        // Theo file mask của bạn, phần ruột chiếm khoảng 70-80% ở giữa.
        val contentLeft = maskWidth * 0.22f
        val contentTop = maskHeight * 0.22f
        val contentRight = maskWidth * 0.78f
        val contentBottom = maskHeight * 0.78f
        val contentRect = RectF(contentLeft, contentTop, contentRight, contentBottom)

        // --- BƯỚC 4: VẼ ẢNH VÀ CẮT THEO HÌNH CHỮ NHẬT ---
        canvas.save()
        val path = Path().apply {
            addRoundRect(contentRect, 10f, 10f, Path.Direction.CW) // Bo góc 10px
        }
        canvas.clipPath(path)
        // Tạo một vùng cắt (Clip) hình chữ nhật để ảnh không bị tràn ra ngoài răng cưa
        canvas.clipRect(contentRect)

        val matrix = Matrix()
        val scale: Float
        val dx: Float
        val dy: Float

        // Center Crop ảnh gốc vào đúng vùng contentRect
        if (originalBitmap.width * contentRect.height() > contentRect.width() * originalBitmap.height) {
            scale = contentRect.height() / originalBitmap.height.toFloat()
            dx = contentRect.left + (contentRect.width() - originalBitmap.width * scale) * 0.5f
            dy = contentRect.top
        } else {
            scale = contentRect.width() / originalBitmap.width.toFloat()
            dx = contentRect.left
            dy = contentRect.top + (contentRect.height() - originalBitmap.height * scale) * 0.5f
        }

        matrix.setScale(scale, scale)
        matrix.postTranslate(dx, dy)

        canvas.drawBitmap(originalBitmap, matrix, Paint(Paint.ANTI_ALIAS_FLAG))
        canvas.restore() // Bỏ vùng clip để vẽ viền

        originalBitmap.recycle()

        // --- BƯỚC 5: VẼ VIỀN RĂNG CƯA ĐÈ LÊN TRÊN ---
        // Đây là bước quan trọng nhất: mask.png sẽ đè lên ảnh,
        // tạo ra hiệu ứng ảnh nằm "trong" khung.
        maskDrawable.setBounds(0, 0, maskWidth, maskHeight)
        maskDrawable.draw(canvas)

        return result
    }

    // --- SỬA LỖI XOAY NGANG ---
    // Hàm tiện ích: Chuyển ImageProxy thành Bitmap và Xoay cho đúng chiều
    private fun imageProxyToCorrectBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        // 1. 解码成原始 Bitmap (解编码为原始 Bitmap (lúc này vẫn có thể xoay ngang)
        val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        // 2. Đọc góc xoay từ Exif data
        var rotationDegrees = image.imageInfo.rotationDegrees

        // 3. Nếu cần xoay, tạo Bitmap mới đã xoay
        if (rotationDegrees != 0) {
            val matrix = Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            val rotated = Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true)
            original.recycle() // 解放原始未旋转 Bitmap (giải phóng Bitmap chưa xoay)
            return rotated
        }

        return original
    }
}