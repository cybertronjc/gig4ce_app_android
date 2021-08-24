package com.gigforce.common_ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*

internal class DrawerView(
    context: Context?,
    attrs: AttributeSet?
) : View(
    context,
    attrs
) {

    companion object{

        const val TAG  = "DrawerView"
    }

    // setup initial color
    private var paintColor = Color.RED

    // defines paint and canvas
    private val drawPaint: MutableList<Paint> = LinkedList()
    private lateinit var tmpPaint: Paint

    // Store circles to draw each time the user touches down
    private val paths: MutableList<Path> = LinkedList()
    private var tmpPath: Path? = null

    private var _maxX: Float = -1.0f
    private var _minX: Float = -1.0f
    private var _maxY: Float = -1.0f
    private var _minY: Float = -1.0f

    val userDrawnRight : Float get() = _maxX
    val userDrawnLeft : Float get() = _minX
    val userDrawnTop : Float get() = _maxY
    val userDrawnBottom : Float get() = _minY

    init {
        setupPaint() // same as before
    }

    fun setColor(color: Int) {
        paintColor = color
        tmpPaint = generatePaint(color)
    }

    private fun generatePaint(color: Int): Paint {
        val paint = Paint()
        paint.color = color
        paint.isAntiAlias = true
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        paint.style = Paint.Style.STROKE // change to fill
        return paint
    }

    // Setup paint with color and stroke styles
    private fun setupPaint() {
        // drawPaint.add(generatePaint(paintColor));
        tmpPaint = generatePaint(paintColor)
    }

    // Draw each circle onto the view
    override fun onDraw(canvas: Canvas) {
        for (i in paths.indices) {
            canvas.drawPath(paths[i], drawPaint[i])
        }
    }

    // Append new circle each time user presses on screen
    override fun onTouchEvent(event: MotionEvent): Boolean {

        val pointX = event.x
        val pointY = event.y
        Log.d(TAG,"new x : $pointX, new y : $pointY, ")

        if (_maxX == -1.0f || pointX > _maxX) {
            _maxX = pointX
            Log.d(TAG,"new Max x : $pointX")
        }

        if (_minX == -1.0f || pointX < _minX){
            _minX = pointX
            Log.d(TAG,"new min x : $pointX")
        }

        if (_maxY == -1.0f || pointY > _maxY) {
            _maxY = pointY
            Log.d(TAG,"new max y : $pointY")
        }

        if (_minY == -1.0f || pointY < _minY) {
            _minY = pointY
            Log.d(TAG,"new min y : $pointY")
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Starts a new line in the path
                tmpPath = Path()
                tmpPath!!.moveTo(pointX, pointY)
            }
            MotionEvent.ACTION_MOVE -> {
                // Draws line between last point and this point
                tmpPath!!.lineTo(pointX, pointY)
                drawPaint.add(tmpPaint)
                paths.add(tmpPath!!)
            }
            else -> return false
        }
        postInvalidate() // Indicate view should be redrawn
        return true // Indicate we've consumed the touch
    }

    fun undoDrawnStuff() {
        drawPaint.clear()
        paths.clear()
        postInvalidate()
    }

    fun hasUserDrawnAnything() = paths.isNotEmpty()
}