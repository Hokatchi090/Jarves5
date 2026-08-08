package com.jarvis.assistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class JarvisDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var tickRotation = 0f
    private var arcSweep = 90f

    private val glowFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#061217")
    }

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#00F6FF")
    }

    private val innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#5FE8F2")
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#00F6FF")
    }

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#FFB800")
    }

    fun setTickRotation(angle: Float) {
        tickRotation = angle
        invalidate()
    }

    fun setArcSweep(sweep: Float) {
        arcSweep = sweep
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = (min(width, height) / 2f) - 16f

        canvas.drawCircle(cx, cy, radius * 0.6f, glowFillPaint)
        canvas.drawCircle(cx, cy, radius, outerRingPaint)
        canvas.drawCircle(cx, cy, radius * 0.78f, innerRingPaint)

        val arcRect = android.graphics.RectF(
            cx - radius * 0.9f, cy - radius * 0.9f,
            cx + radius * 0.9f, cy + radius * 0.9f
        )
        canvas.drawArc(arcRect, tickRotation, arcSweep, false, arcPaint)

        canvas.save()
        canvas.rotate(tickRotation, cx, cy)
        for (i in 0 until 24) {
            val angle = Math.toRadians((i * 15).toDouble())
            val innerR = radius * 0.86f
            val outerR = radius * 0.98f
            val x1 = (cx + innerR * cos(angle)).toFloat()
            val y1 = (cy + innerR * sin(angle)).toFloat()
            val x2 = (cx + outerR * cos(angle)).toFloat()
            val y2 = (cy + outerR * sin(angle)).toFloat()
            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }
        canvas.restore()
    }
}
