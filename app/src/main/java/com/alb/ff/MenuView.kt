package com.alb.ff

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

data class Feature(val name: String, var enabled: Boolean = false)

class MenuView(context: Context) : View(context) {

    val features = listOf(
        Feature("Aimbot Head"),
        Feature("ESP Box"),
        Feature("ESP Ten"),
        Feature("ESP Distance"),
        Feature("No Recoil"),
        Feature("Fast Heal"),
        Feature("FOV Circle"),
    )

    val menuW  = 260f
    val header = 44f
    val rowH   = 44f
    val menuH  get() = header + features.size * rowH + 12f

    // callback khi kéo — OverlayService cập nhật WM
    var onDrag: ((dx: Float, dy: Float) -> Unit)? = null

    private var lastX = 0f
    private var lastY = 0f
    private var isDragging = false

    private val bgPaint = Paint().apply {
        color = Color.argb(220, 8, 8, 18)
    }
    private val borderPaint = Paint().apply {
        color = Color.argb(255, 0, 210, 100)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val titlePaint = Paint().apply {
        color = Color.WHITE
        textSize = 20f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    private val onPaint = Paint().apply {
        color = Color.argb(255, 0, 230, 100)
        textSize = 17f
        isAntiAlias = true
    }
    private val offPaint = Paint().apply {
        color = Color.argb(180, 150, 150, 150)
        textSize = 17f
        isAntiAlias = true
    }
    private val dotOn = Paint().apply {
        color = Color.argb(255, 0, 230, 100)
        isAntiAlias = true
    }
    private val dotOff = Paint().apply {
        color = Color.argb(180, 90, 90, 90)
        isAntiAlias = true
    }
    private val divPaint = Paint().apply {
        color = Color.argb(35, 255, 255, 255)
        strokeWidth = 1f
    }

    override fun onDraw(canvas: Canvas) {
        val rect = RectF(0f, 0f, menuW, menuH)
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)
        canvas.drawRoundRect(rect, 14f, 14f, borderPaint)
        canvas.drawText("ALB  FF  OB55", 16f, 30f, titlePaint)

        features.forEachIndexed { i, feat ->
            val ry = header + i * rowH
            canvas.drawCircle(20f, ry + 20f,
                6f, if (feat.enabled) dotOn else dotOff)
            canvas.drawText(feat.name, 36f, ry + 27f,
                if (feat.enabled) onPaint else offPaint)
            canvas.drawLine(10f, ry + rowH - 1f,
                menuW - 10f, ry + rowH - 1f, divPaint)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.rawX
                lastY = ev.rawY
                isDragging = ev.y < header
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    onDrag?.invoke(ev.rawX - lastX, ev.rawY - lastY)
                    lastX = ev.rawX
                    lastY = ev.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val idx = ((ev.y - header) / rowH).toInt()
                    if (idx in features.indices) {
                        features[idx].enabled = !features[idx].enabled
                        invalidate()
                    }
                }
                isDragging = false
                return true
            }
        }
        return false
    }
}
