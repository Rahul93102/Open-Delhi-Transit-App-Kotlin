package com.example.opendelhitransit.features.steptracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.abs

class CanvasView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var points: ArrayList<Pair<Double, Double>> = ArrayList()
    private var resetPointsFlag: Boolean = false
    private var scaleFactor: Float = 1.0f
    private val paint: Paint = Paint().apply {
        color = Color.RED
        strokeWidth = 12.0f
        style = Paint.Style.STROKE
    }

    /**
     * Update the data for drawing on the canvas
     *
     * @param dx The x-coordinate displacement
     * @param dy The y-coordinate displacement
     * @param reset Flag to reset the points
     */
    fun updateData(dx: Double, dy: Double, reset: Boolean) {
        if (reset) {
            points.clear()
            points.add(Pair(dx, dy))
            resetPointsFlag = true
            invalidate()
            return
        }

        val lastPoint = points.last()
        val newX = lastPoint.first + dx
        val newY = lastPoint.second + dy
        points.add(Pair(newX, newY))
        invalidate()
    }

    /**
     * Set the scale factor for the canvas
     *
     * @param factor The scale factor to set
     */
    fun setScaleFactor(factor: Float) {
        scaleFactor = factor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (points.isEmpty()) {
            val center_x = (width / 2).toDouble()
            val center_y = (height / 2).toDouble()
            points.add(Pair(center_x, center_y))
            resetPointsFlag = true
        }

        canvas.scale(scaleFactor, scaleFactor, (width / 2).toFloat(), (height / 2).toFloat())

        if (resetPointsFlag) {
            canvas.drawPoint(points[0].first.toFloat(), points[0].second.toFloat(), paint)
            resetPointsFlag = false
        } else {
            for (i in 1 until points.size) {
                val startX = points[i - 1].first.toFloat()
                val startY = points[i - 1].second.toFloat()
                val endX = points[i].first.toFloat()
                val endY = points[i].second.toFloat()

                if (abs(startX - endX) > 1000 || abs(startY - endY) > 1000) {
                    continue
                }

                canvas.drawLine(startX, startY, endX, endY, paint)
            }
        }
    }
} 