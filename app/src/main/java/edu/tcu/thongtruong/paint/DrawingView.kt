package edu.tcu.thongtruong.paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private val paint = Paint()
    private var path = CustomPath(Color.BLACK, 10 * resources.displayMetrics.density)
    private val pathList = mutableListOf<CustomPath>()

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        return when(event.action){
            MotionEvent.ACTION_DOWN ->{
                pathList.add(path)
                path.moveTo(x,y)
                true
            }
            MotionEvent.ACTION_MOVE ->{
                path.lineTo(x,y)
                invalidate()
                true
            }
            MotionEvent.ACTION_UP ->{
                path = CustomPath(path.color,path.width)
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        for(path in pathList){
            paint.color = path.color
            paint.strokeWidth = path.width
            canvas.drawPath(path,paint)
        }
    }

    private data class CustomPath(val color: Int, val width: Float):Path()

    fun setPathColor(color: Int) {
        path = CustomPath(color, path.width)
    }

    fun setPathWidth(width: Int) {
        path = CustomPath(path.color, width.toFloat() * resources.displayMetrics.density)
    }

    fun undoPath() {
        if (pathList.isNotEmpty()) {
            pathList.removeLast()
            invalidate()
        }
    }

}