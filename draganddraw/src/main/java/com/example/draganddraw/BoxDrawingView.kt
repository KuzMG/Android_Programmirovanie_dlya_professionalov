package com.example.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

private const val TAG = "BoxDrawingView"
private const val KEY_BOX_START = "boxen_start"
private const val KEY_BOX_END = "boxen_end"
private const val KEY_STATE = "state"

class BoxDrawingView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var currentBox: Box? = null
    private val boxen = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000.toInt()
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }
    val p = Path()
    val m = Matrix()

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                currentBox = Box(current).also {
                    boxen.add(it)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                updateCurrentBox(current)
                if (event.pointerCount == 2) {
                    updateRotateCurrentBox(event.getX(1))
                }
            }

            MotionEvent.ACTION_UP -> {
                updateCurrentBox(current)
                currentBox = null
            }

            MotionEvent.ACTION_CANCEL -> {
                currentBox = null
            }

            MotionEvent.ACTION_POINTER_UP -> {

            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                currentBox?.xStart = event.getX(1)
            }

        }
        return true
    }

    private fun updateCurrentBox(current: PointF) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    private fun updateRotateCurrentBox(currentX: Float) {
        currentBox?.let {
            it.xEnd = currentX
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPaint(backgroundPaint)
        boxen.forEach { box ->
            p.reset()
            p.addRect(box.left, box.top, box.right, box.bottom,Path.Direction.CW)
            m.reset()
            m.setRotate(box.rotate,(box.left+box.right)/2,(box.top+box.bottom)/2)
            p.transform(m)
            canvas.drawPath(p,boxPaint)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        val pointsStart = boxen.map {
            it.start
        }.toTypedArray()
        val pointsEnd = boxen.map {
            it.end
        }.toTypedArray()
        bundle.putParcelableArray(KEY_BOX_START, pointsStart)
        bundle.putParcelableArray(KEY_BOX_END, pointsEnd)
        bundle.putParcelable(KEY_STATE, super.onSaveInstanceState())
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        val pointsStart = bundle.getParcelableArray(KEY_BOX_START) as Array<PointF>
        val pointsEnd = bundle.getParcelableArray(KEY_BOX_END) as Array<PointF>
        for (i in 0 until pointsStart.size) {
            val box = Box(pointsStart[i])
            box.end = pointsEnd[i]
            boxen.add(box)
        }

        val primaryState: Parcelable = bundle.getParcelable<Parcelable>(KEY_STATE)!!
        super.onRestoreInstanceState(primaryState)
    }
}