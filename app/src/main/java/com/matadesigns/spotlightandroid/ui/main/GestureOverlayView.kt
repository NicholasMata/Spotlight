package com.matadesigns.spotlightandroid.ui.main

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.ViewOutlineProvider
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.abstraction.SpotlightAnimatable
import com.matadesigns.spotlight.abstraction.SpotlightTargetOverlay


enum class GestureType {
    tap, pullDown
}

class GestureOverlayView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SpotlightTargetOverlay, SpotlightAnimatable {
    override var spotlightView: SpotlightView? = null
    var minRadius: Float
    var maxRadius: Float
    private var _innerRadius: Float
    private var _outerRadius: Float
    protected val _innerDotPaint = Paint()
    protected val _outerDotPaint = Paint()
    var animators = mutableListOf<Animator>()

    var dotX: Float = -1f
    var dotY: Float = -1f

    var gestureType: GestureType = GestureType.tap
        set(value) {
            field = value
            setAlphas()
        }

    init {
        clipToOutline = false
        setWillNotDraw(false)

        outlineProvider = ViewOutlineProvider.BACKGROUND
        _innerDotPaint.style = Paint.Style.FILL
        _innerDotPaint.color = Color.parseColor("#FFFFFF")
        _innerDotPaint.isAntiAlias = true

        _outerDotPaint.style = Paint.Style.FILL
        _outerDotPaint.color = Color.parseColor("#FFFFFF")
        _outerDotPaint.isAntiAlias = true

        minRadius = 20 * context.resources.displayMetrics.density
        maxRadius = 24 * context.resources.displayMetrics.density
        _innerRadius = minRadius
        _outerRadius = minRadius

        setAlphas()
    }

    fun setAlphas() {
        when(gestureType) {
            GestureType.pullDown -> {
                _outerDotPaint.alpha = 50
                _innerDotPaint.alpha = 100
            }
            GestureType.tap -> {
                _outerDotPaint.alpha = 50
                _innerDotPaint.alpha = 100
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        dotX = width / 2f
        if (gestureType == GestureType.tap) {
            dotY = height / 2f
        }
        canvas?.drawCircle(dotX, dotY, minRadius, _innerDotPaint)
        canvas?.drawCircle(dotX, dotY, maxRadius, _outerDotPaint)
    }

    override fun endAnimation() {
        animators.forEach { it.cancel() }
        animators.clear()
    }

    override fun startAnimation() {

        when (gestureType) {
            GestureType.pullDown -> {
                val toY = Math.min(height.toFloat(), 150 * context.resources.displayMetrics.density)
                val yAnimator = ValueAnimator.ofFloat(10f, toY)
                yAnimator.addUpdateListener {
                    val value = it.animatedValue as Float
                    dotY = value
                    _innerDotPaint.alpha = (100 * (1 - (value / toY))).toInt()
                    _outerDotPaint.alpha = (50 * (1 - (value / toY))).toInt()
                    postInvalidate()
                }

                yAnimator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        if(animators.size == 0) return
                        yAnimator.setStartDelay(1000);
                        yAnimator.start();
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }

                })
                yAnimator.duration = 2500
                yAnimator.start()
                animators.add(yAnimator)
            }
            GestureType.tap -> {
                val outerAnimator = ValueAnimator.ofInt(50, 0)
                outerAnimator.addUpdateListener {
                    val value = it.animatedValue as Int
                    _outerDotPaint.alpha = value
                    postInvalidate()
                }
                outerAnimator.duration = 2000
                outerAnimator.repeatMode = REVERSE
                outerAnimator.repeatCount = INFINITE
                outerAnimator.start()

                val innerAnimator = ValueAnimator.ofInt(100, 50)
                innerAnimator.addUpdateListener {
                    _innerDotPaint.alpha = it.animatedValue as Int
                    postInvalidate()
                }
                innerAnimator.duration = 2000
                innerAnimator.repeatMode = REVERSE
                innerAnimator.repeatCount = INFINITE
                innerAnimator.start()

                animators.add(outerAnimator)
                animators.add(innerAnimator)
            }
        }
    }

}