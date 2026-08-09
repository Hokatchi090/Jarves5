package com.jarvis.assistant

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin
import kotlin.random.Random

class JarvisDialView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val cyan = Color.rgb(0, 235, 255)
    private val cyanSoft = Color.rgb(80, 210, 225)

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val particlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
    }

    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val random = Random(91827)

    private data class Particle(
        val x: Float,
        val y: Float,
        val size: Float,
        val speed: Float,
        val phase: Float
    )

    private val particles = ArrayList<Particle>()

    private var elapsed = 0f
    private var voiceLevel = 0f
    private var speaking = false

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        repeat(55) {
            particles.add(
                Particle(
                    x = random.nextFloat() * 2f - 1f,
                    y = random.nextFloat() * 2f - 1f,
                    size = 1.0f + random.nextFloat() * 2.5f,
                    speed = 0.08f + random.nextFloat() * 0.20f,
                    phase = random.nextFloat() * 6.28f
                )
            )
        }
    }

    fun setVoiceLevel(level: Float) {
        voiceLevel = level.coerceIn(0f, 1f)
        invalidate()
    }

    fun setSpeaking(value: Boolean) {
        speaking = value
        if (!value) {
            voiceLevel *= 0.5f
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        elapsed += 0.016f

        val cx = width / 2f
        val cy = height / 2f
        val base = minOf(width, height) * 0.46f

        drawParticles(canvas, cx, cy, base)
        drawHolographicRings(canvas, cx, cy, base)
        drawCore(canvas, cx, cy, base)
        drawJarvisText(canvas, cx, cy, base)

        postInvalidateOnAnimation()
    }

    private fun drawHolographicRings(canvas: Canvas, cx: Float, cy: Float, base: Float) {
        val energy = if (speaking) {
            1f + voiceLevel * 0.30f
        } else {
            1f + 0.04f * sin(elapsed * 2.0)
        }

        val rings = arrayOf(
            floatArrayOf(0.35f, 0.80f, 1.0f, 0.0f),
            floatArrayOf(0.48f, 0.92f, -0.72f, 1.0f),
            floatArrayOf(0.60f, 1.05f, 0.48f, -1.0f),
            floatArrayOf(0.72f, 1.18f, -0.25f, 1.0f),
            floatArrayOf(0.82f, 1.32f, 0.72f, -1.0f),
            floatArrayOf(0.93f, 1.45f, -0.52f, 1.0f)
        )

        rings.forEachIndexed { index, ring ->
            val rx = base * ring[0] * energy
            val ry = base * ring[1] * energy
            val tilt = ring[2]
            val direction = ring[3]
            val rotation = elapsed * (18f + index * 5f) * direction

            canvas.save()
            canvas.translate(0f, -sin(elapsed * 0.65f + index) * 10f)
            canvas.rotate(Math.toDegrees(tilt.toDouble()).toFloat(), cx, cy)
            canvas.scale(1f, 0.48f + index * 0.025f, cx, cy)

            val rect = RectF(cx - rx, cy - ry, cx + rx, cy + ry)

            ringPaint.strokeWidth = if (index % 2 == 0) 2.0f else 1.2f
            ringPaint.alpha = if (index % 2 == 0) 145 else 90
            ringPaint.color = if (index % 2 == 0) cyan else cyanSoft

            val start = rotation % 360f
            val sweep = when (index % 3) {
                0 -> 250f
                1 -> 185f
                else -> 310f
            }

            canvas.drawArc(rect, start, sweep, false, ringPaint)
            canvas.drawArc(rect, start + 190f, sweep * 0.32f, false, ringPaint)

            drawSegments(canvas, cx, cy, rx, ry, rotation, index)

            canvas.restore()
        }
    }

    private fun drawSegments(
        canvas: Canvas, cx: Float, cy: Float,
        rx: Float, ry: Float, rotation: Float, index: Int
    ) {
        segmentPaint.color = cyan
        segmentPaint.alpha = 190
        segmentPaint.strokeWidth = 2.5f

        for (i in 0 until 5) {
            val start = rotation + i * 72f + index * 17f
            val sweep = if (i % 2 == 0) 9f else 17f
            val rect = RectF(cx - rx, cy - ry, cx + rx, cy + ry)
            canvas.drawArc(rect, start, sweep, false, segmentPaint)
        }
    }

    private fun drawParticles(canvas: Canvas, cx: Float, cy: Float, base: Float) {
        particles.forEachIndexed { index, p ->
            val drift = (elapsed * p.speed * 60f + index * 8f) % 150f
            val normalizedY = p.y + drift / base
            val x = cx + p.x * base + sin(elapsed * 0.8f + p.phase) * 8f
            val y = cy + normalizedY * base

            if (y < cy + base * 1.5f) {
                particlePaint.color = cyan
                particlePaint.alpha =
                    (35 + 80 * sin(elapsed * 2f + p.phase).coerceAtLeast(0f)).toInt()
                canvas.drawCircle(x, y, p.size, particlePaint)
            }
        }
    }

    private fun drawCore(canvas: Canvas, cx: Float, cy: Float, base: Float) {
        val pulse = if (speaking) {
            1f + voiceLevel * 0.45f
        } else {
            1f + 0.07f * sin(elapsed * 2.4f)
        }
        val radius = base * 0.17f * pulse

        glowPaint.color = cyan
        glowPaint.alpha = if (speaking) 55 else 28
        glowPaint.setShadowLayer(base * 0.12f, 0f, 0f, cyan)
        canvas.drawCircle(cx, cy, radius * 1.65f, glowPaint)
        glowPaint.clearShadowLayer()

        corePaint.color = Color.rgb(7, 27, 32)
        corePaint.alpha = 235
        canvas.drawCircle(cx, cy, radius, corePaint)

        ringPaint.color = cyan
        ringPaint.alpha = 210
        ringPaint.strokeWidth = 1.5f
        canvas.drawCircle(cx, cy, radius * 1.28f, ringPaint)
    }

    private fun drawJarvisText(canvas: Canvas, cx: Float, cy: Float, base: Float) {
        val pulse = if (speaking) {
            1f + voiceLevel * 0.18f
        } else {
            1f + 0.035f * sin(elapsed * 2.4f)
        }

        textPaint.textSize = base * 0.18f * pulse
        textPaint.color = Color.WHITE
        textPaint.alpha = 245
        textPaint.setShadowLayer(base * 0.045f, 0f, 0f, cyan)

        val baseline = cy - (textPaint.ascent() + textPaint.descent()) / 2f
        canvas.drawText("\u062C\u0627\u0631\u0641\u0633", cx, baseline, textPaint)

        textPaint.clearShadowLayer()
    }
}
