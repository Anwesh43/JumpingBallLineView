package com.anwesh.uiprojects.jumpingballlineview

/**
 * Created by anweshmishra on 17/10/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val sizeFactor : Float = 2.9f
val strokeFactor : Int = 90
val delay : Long = 30
val scGap : Float = 0.01f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val maxDeg : Float = 180f
val rFactor : Float = 4f
val yOffsetFactor : Float = 0.4f

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawJumpingBallLine(scale : Float, h : Float, size : Float, paint : Paint) {
    val r : Float = size / rFactor
    val deg : Float = 180f * scale.divideScale(0, 2)
    val y : Float = -(h - size) * Math.sin(deg * Math.PI / 180).toFloat()
    val y1 : Float = h * scale.divideScale(1, 2)
    save()
    translate(0f, y)
    drawCircle(0f, 0f, r, paint)
    restore()
    save()
    translate(0f, y1)
    drawLine(0f, -size, 0f, size, paint)
    restore()
}

fun Canvas.drawJBLNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), h / 2)
    drawJumpingBallLine(scale, h * yOffsetFactor, size, paint)
    restore()
}
