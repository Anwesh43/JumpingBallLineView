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
val sizeFactor : Float = 2.5f
val strokeFactor : Int = 90
val delay : Long = 30
val scGap : Float = 0.01f
val foreColor : Int = Color.parseColor("#4CAF50")
val backColor : Int = Color.parseColor("#BDBDBD")
val maxDeg : Float = 180f
val rFactor : Float = 2f
val yOffsetFactor : Float = 0.4f
val PIRADIAN : Double = Math.PI / maxDeg

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

fun Canvas.drawJumpingBallLine(scale : Float, h : Float, size : Float, paint : Paint) {
    val r : Float = size / rFactor
    val deg1 : Float = maxDeg * scale.divideScale(0, 2)
    val deg2 : Float = maxDeg * scale.divideScale(1, 2)
    val y : Float = -(h - 2 * r) * Math.sin(deg1 * PIRADIAN).toFloat()
    val y1 : Float = h * Math.sin(deg2 * PIRADIAN).toFloat()
    save()
    translate(0f, y - r)
    drawCircle(0f, 0f, r, paint)
    restore()
    drawLine(0f, 0f, 0f, y1, paint)
    save()
    translate(0f, y1)
    drawLine(-size, 0f, size, 0f, paint)
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

class JumpingBallLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class JBLNode(var i : Int, val state : State = State()) {

        private var next : JBLNode? = null
        private var prev : JBLNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = JBLNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawJBLNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : JBLNode {
            var curr : JBLNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class JumpingBallLine(var i : Int) {

        private val root : JBLNode = JBLNode(0)
        private var curr : JBLNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : JumpingBallLineView) {

        private val jbl : JumpingBallLine = JumpingBallLine(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            jbl.draw(canvas, paint)
            animator.animate {
                jbl.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            jbl.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : JumpingBallLineView {
            val view : JumpingBallLineView = JumpingBallLineView(activity)
            activity.setContentView(view)
            return view
        }
    }
}