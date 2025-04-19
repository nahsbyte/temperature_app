package com.monitoring.farmasidinkesminahasa.interfaces

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.monitoring.farmasidinkesminahasa.R

// Custom Marker View for Highlighting
class CustomMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource){

    private val paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 3f
        isAntiAlias = true
    }

    // Add a reference to the TextView for displaying the Timestamp
    private val timestampTextView: TextView = findViewById(R.id.markerText)

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        super.draw(canvas, posX, posY)

        // Ensure the types are compatible
        val startY = 0f
        val endY = height.toFloat()

        // Draw a vertical yellow line at the highlighted position (posX)
        canvas.drawLine(posX, startY, posX, endY, paint)
    }

    // Update the Timestamp in the marker's TextView
    fun updateTimestamp(timestamp: String) {
        timestampTextView.text = timestamp
    }
}