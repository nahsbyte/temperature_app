package com.monitoring.farmasidinkesminahasa.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt() // Warna latar belakang lingkaran (abu-abu)
        style = Paint.Style.STROKE
        strokeWidth = 20f // Ketebalan lingkaran
    }

    private val paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF76C7C0.toInt() // Warna progres (biru kehijauan)
        style = Paint.Style.STROKE
        strokeWidth = 20f // Ketebalan lingkaran
        strokeCap = Paint.Cap.ROUND // Ujung progres membulat
    }

    private var progress = 0f // Progress dalam persen (0-100)

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f) // Pastikan nilai antara 0 dan 100
        invalidate() // Meminta ulang gambar
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val radius = (width.coerceAtMost(height) / 2) - 20 // Radius lingkaran
        val centerX = width / 2f
        val centerY = height / 2f

        // Gambar lingkaran latar belakang
        canvas.drawCircle(centerX, centerY, radius.toFloat(), paintBackground)

        // Gambar lingkaran progres
        val sweepAngle = (progress / 100) * 360
        canvas.drawArc(
            centerX - radius, centerY - radius, centerX + radius, centerY + radius,
            -90f, sweepAngle, false, paintProgress
        )
    }
}
