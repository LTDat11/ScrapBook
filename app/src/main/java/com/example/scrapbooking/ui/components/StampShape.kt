package com.example.scrapbooking.ui.components

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Hình dạng "tem bưu chính" với viền răng cưa bán nguyệt.
 *
 * @param hPerforations  Số ô bán nguyệt trên cạnh ngang (trên + dưới). Mặc định 7.
 * @param vPerforations  Số ô bán nguyệt trên cạnh dọc (trái + phải). Mặc định 9.
 * @param perforationRadius Bán kính của mỗi bán nguyệt.
 */
class StampShape(
    private val hPerforations: Int = 7,
    private val vPerforations: Int = 9,
    private val perforationRadius: Dp = 5.5.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { perforationRadius.toPx() }
        return Outline.Generic(buildStampPath(size, r))
    }

    /**
     * Dựng path theo chiều kim đồng hồ (trên → phải → dưới → trái).
     *
     * Mỗi bán nguyệt khoét VÀO bên trong stamp:
     *  - Cạnh trên  : bán nguyệt bổ sung hướng XUỐNG  (start=180°, sweep=-180°)
     *  - Cạnh phải  : bán nguyệt khoét hướng SANG TRÁI (start=270°, sweep=-180°)
     *  - Cạnh dưới  : bán nguyệt khoét hướng LÊN TRÊN (start=0°,   sweep=-180°)
     *  - Cạnh trái  : bán nguyệt khoét hướng SANG PHẢI (start=90°,  sweep=-180°)
     *
     * sweep=-180° = ngược chiều kim đồng hồ trong toạ độ màn hình (y↓),
     * điều này khiến cung đi qua điểm "trong" stamp.
     */
    private fun buildStampPath(size: Size, r: Float): Path {
        val w = size.width
        val h = size.height
        val path = Path()

        val hStep = w / hPerforations.toFloat()
        val vStep = h / vPerforations.toFloat()

        // ── Cạnh TRÊN : trái → phải, bán nguyệt khoét xuống ──
        path.moveTo(0f, 0f)
        for (i in 0 until hPerforations) {
            val cx = hStep * i + hStep / 2f
            path.arcTo(
                rect = Rect(cx - r, -r, cx + r, r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
        }
        path.lineTo(w, 0f)

        // ── Cạnh PHẢI : trên → dưới, bán nguyệt khoét sang trái ──
        for (i in 0 until vPerforations) {
            val cy = vStep * i + vStep / 2f
            path.arcTo(
                rect = Rect(w - r, cy - r, w + r, cy + r),
                startAngleDegrees = 270f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
        }
        path.lineTo(w, h)

        // ── Cạnh DƯỚI : phải → trái, bán nguyệt khoét lên trên ──
        for (i in hPerforations - 1 downTo 0) {
            val cx = hStep * i + hStep / 2f
            path.arcTo(
                rect = Rect(cx - r, h - r, cx + r, h + r),
                startAngleDegrees = 0f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
        }
        path.lineTo(0f, h)

        // ── Cạnh TRÁI : dưới → trên, bán nguyệt khoét sang phải ──
        for (i in vPerforations - 1 downTo 0) {
            val cy = vStep * i + vStep / 2f
            path.arcTo(
                rect = Rect(-r, cy - r, r, cy + r),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
        }
        path.close()

        return path
    }
}
