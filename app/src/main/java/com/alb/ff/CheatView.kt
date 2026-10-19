package com.alb.ff

import android.content.Context
import android.graphics.*
import android.view.View

class CheatView(context: Context, private val menu: MenuView) : View(context) {

    data class Entity(
        val sx: Float, val sy: Float,
        val height: Float, val dist: Float
    )

    var entities: List<Entity> = emptyList()
    var aimTarget: Pair<Float, Float>? = null

    private val boxPaint  = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 2f; isAntiAlias = true }
    private val acPaint   = Paint().apply { color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 3f; isAntiAlias = true }
    private val snapPaint = Paint().apply { color = Color.argb(60, 255, 255, 255); strokeWidth = 1f }
    private val namePaint = Paint().apply { color = Color.WHITE; textSize = 20f; isAntiAlias = true }
    private val distPaint = Paint().apply { color = Color.argb(210, 255, 255, 255); textSize = 16f; isAntiAlias = true }
    private val aimPaint  = Paint().apply { color = Color.RED; strokeWidth = 3f; isAntiAlias = true }
    private val fovPaint  = Paint().apply {
        color = Color.argb(55, 255, 255, 255)
        style = Paint.Style.STROKE; strokeWidth = 1.5f; isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        val f = menu.features
        val espBox  = f.getOrNull(1)?.enabled == true
        val espName = f.getOrNull(2)?.enabled == true
        val espDist = f.getOrNull(3)?.enabled == true
        val fovOn   = f.getOrNull(6)?.enabled == true

        if (fovOn) {
            canvas.drawCircle(width / 2f, height * 0.45f, 350f, fovPaint)
        }

        for (e in entities) {
            val color = if (e.dist < 50)
                Color.argb(255, 255, 60, 60)
            else
                Color.argb(255, 0, 220, 100)
            boxPaint.color = color

            val bw   = e.height * 0.4f
            val left = e.sx - bw / 2f
            val top  = e.sy - e.height
            val right  = e.sx + bw / 2f
            val bottom = e.sy

            if (espBox) {
                canvas.drawRect(left, top, right, bottom, boxPaint)
                val ca = bw * 0.25f
                // corner top-left
                canvas.drawLine(left, top, left + ca, top, acPaint)
                canvas.drawLine(left, top, left, top + ca, acPaint)
                // corner top-right
                canvas.drawLine(right, top, right - ca, top, acPaint)
                canvas.drawLine(right, top, right, top + ca, acPaint)
                // corner bot-left
                canvas.drawLine(left, bottom, left + ca, bottom, acPaint)
                canvas.drawLine(left, bottom, left, bottom - ca, acPaint)
                // corner bot-right
                canvas.drawLine(right, bottom, right - ca, bottom, acPaint)
                canvas.drawLine(right, bottom, right, bottom - ca, acPaint)
                // snapline
                canvas.drawLine(width / 2f, height.toFloat(), e.sx, e.sy, snapPaint)
            }

            if (espName) {
                namePaint.color = color
                canvas.drawText("Enemy", e.sx - 22f, top - 8f, namePaint)
            }

            if (espDist) {
                canvas.drawText("%.0fm".format(e.dist), e.sx - 14f, bottom + 20f, distPaint)
            }
        }

        aimTarget?.let { (ax, ay) ->
            canvas.drawLine(ax - 12f, ay, ax + 12f, ay, aimPaint)
            canvas.drawLine(ax, ay - 12f, ax, ay + 12f, aimPaint)
        }
    }
}
