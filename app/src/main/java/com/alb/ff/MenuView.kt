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

    private var posX = 30f
    private var posY = 200f
    private var dragOffX = 0f
    private var dragOffY = 0f
    private var dragging = false

    private val W      = 260f
    private val HEADER = 44f
    private val ROW    = 44f
    private val H get() = HEADER + features.size * ROW + 12f

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
        val rect = RectF(posX, posY, posX + W, posY + H)
        canvas.drawRoundRect(rect, 14f, 14f, bgPaint)
        canvas.drawRoundRect(rect, 14f, 14f, borderPaint)
        canvas.drawText("ALB  FF  OB55", posX + 16f, posY + 30f, titlePaint)

        features.forEachIndexed { i, feat ->
            val ry = posY + HEADER + i * ROW
            canvas.drawCircle(posX + 20f, ry + 20f,
                6f, if (feat.enabled) dotOn else dotOff)
            canvas.drawText(feat.name, posX + 36f, ry + 27f,
                if (feat.enabled) onPaint else offPaint)
            canvas.drawLine(posX + 10f, ry + ROW - 1f,
                posX + W - 10f, ry + ROW - 1f, divPaint)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val tx = ev.rawX; val ty = ev.rawY
        fun inBounds() = tx in posX..(posX + W) && ty in posY..(posY + H)

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!inBounds()) return false
                if (ty < posY + HEADER) {
                    dragging = true
                    dragOffX = tx - posX
                    dragOffY = ty - posY
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    posX = tx - dragOffX
                    posY = ty - dragOffY
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (dragging) { dragging = false; return true }
                if (inBounds()) {
                    val idx = ((ty - posY - HEADER) / ROW).toInt()
                    if (idx in features.indices) {
                        features[idx].enabled = !features[idx].enabled
                        invalidate()
                    }
                }
                return true
            }
        }
        return false
    }
}
