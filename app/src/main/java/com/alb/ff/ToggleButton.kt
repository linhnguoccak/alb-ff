package com.alb.ff

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class ToggleButton(context: Context) : View(context) {

    var onTap: (() -> Unit)? = null
    var onDrag: ((dx: Float, dy: Float) -> Unit)? = null
    var menuVisible = true

    private var lastX  = 0f
    private var lastY  = 0f
    private var moved  = false

    private val bgOn  = Paint().apply { color = Color.argb(220, 0, 180, 80);  isAntiAlias = true }
    private val bgOff = Paint().apply { color = Color.argb(220, 180, 30, 30); isAntiAlias = true }
    private val txtP  = Paint().apply {
        color = Color.WHITE; textSize = 18f
        typeface = Typeface.DEFAULT_BOLD; isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(w: Int, h: Int) = setMeasuredDimension(72, 72)

    override fun onDraw(canvas: Canvas) {
        val p = if (menuVisible) bgOn else bgOff
        canvas.drawRoundRect(RectF(0f, 0f, 72f, 72f), 18f, 18f, p)
        canvas.drawText(if (menuVisible) "ON" else "OFF", 36f, 44f, txtP)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = ev.rawX; lastY = ev.rawY; moved = false; return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = ev.rawX - lastX; val dy = ev.rawY - lastY
                if (Math.hypot(dx.toDouble(), dy.toDouble()) > 8) {
                    moved = true
                    onDrag?.invoke(dx, dy)
                    lastX = ev.rawX; lastY = ev.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (!moved) onTap?.invoke()
                return true
            }
        }
        return false
    }
}
