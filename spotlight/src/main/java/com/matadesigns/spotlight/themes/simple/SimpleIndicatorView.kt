package com.matadesigns.spotlight.themes.simple

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.matadesigns.spotlight.abstraction.SpotlightAnimatable
import com.matadesigns.spotlight.abstraction.SpotlightIndicator
import com.matadesigns.spotlight.config.SpotlightMessageGravity

open class SimpleIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), SpotlightIndicator, SpotlightAnimatable {

    protected val _start = PointF()
    protected val _end = PointF()
    protected var _dotOuterRadius = 0f
    protected var _dotInnerRadius = 0f

    override var spotlightGravity = SpotlightMessageGravity.default

    protected val _linePaint = Paint()
    protected val _outerDotPaint = Paint()
    protected val _innerDotPaint = Paint()

    public var innerDotColor = -0x333334
        set(value) {
            field = value
            _innerDotPaint.color = value
            postInvalidate()
        }

    public var outerDotColor: Int = -1
        set(value) {
            field = value
            _outerDotPaint.setColor(value)
            postInvalidate()
        }
    public var outerDotStrokeWidth: Float = context.resources.displayMetrics.density * 3
        set(value) {
            field = value
            _outerDotPaint.strokeWidth = value
            postInvalidate()
        }

    public var lineWidth: Float = context.resources.displayMetrics.density * 3
        set(value) {
            field = value
            _linePaint.strokeWidth = value
            postInvalidate()
        }
    public var lineColor: Int = -1
        set(value) {
            field = value
            _linePaint.setColor(value)
            postInvalidate()
        }

    public var lineAnimationDuration: Long = 400
    public var dotAnimationDuration: Long = 600

    public var dotRadius = context.resources.displayMetrics.density * 6
        set(value) {
            field = value
            postInvalidate()
        }

    init {
        clipToOutline = false

        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
        val backgroundColor = typedValue.data
        outerDotColor = backgroundColor
        lineColor = backgroundColor

        _linePaint.style = Paint.Style.FILL
        _linePaint.color = lineColor
        _linePaint.strokeWidth = lineWidth
        _linePaint.isAntiAlias = true

        _outerDotPaint.style = Paint.Style.STROKE
        _outerDotPaint.color = outerDotColor
        _outerDotPaint.strokeCap = Paint.Cap.ROUND
        _outerDotPaint.strokeWidth = outerDotStrokeWidth
        _outerDotPaint.isAntiAlias = true

        _innerDotPaint.style = Paint.Style.FILL
        _innerDotPaint.color = innerDotColor
        _innerDotPaint.isAntiAlias = true
    }

    override fun startAnimation() {
        val start = getLineStart()
        _start.set(start)
        _end.set(start)
        val actualEnd = getLineEnd()
        val dotDiameter = dotRadius + outerDotStrokeWidth
        val dotAnimator = ValueAnimator.ofFloat(0f, dotRadius)
        dotAnimator.addUpdateListener {
            val animatedValue = dotAnimator.animatedValue as Float
            _dotOuterRadius = animatedValue
            _dotInnerRadius = animatedValue
            postInvalidate()
        }
        val lineAnimator = when (spotlightGravity) {
            SpotlightMessageGravity.right -> {
                ValueAnimator.ofFloat(_end.x, actualEnd.x + dotDiameter)
            }
            SpotlightMessageGravity.left -> {
                ValueAnimator.ofFloat(_end.x, actualEnd.x - dotDiameter)
            }
            SpotlightMessageGravity.bottom -> {
                ValueAnimator.ofFloat(_end.y, actualEnd.y + dotDiameter)
            }
            SpotlightMessageGravity.top -> {
                ValueAnimator.ofFloat(_end.y, actualEnd.y - dotDiameter)
            }
        }

        lineAnimator.addUpdateListener {
            when (spotlightGravity) {
                SpotlightMessageGravity.right, SpotlightMessageGravity.left -> {
                    _end.x = it.animatedValue as Float
                }
                SpotlightMessageGravity.bottom, SpotlightMessageGravity.top -> {
                    _end.y = it.animatedValue as Float
                }
            }
            postInvalidate()
        }

        lineAnimator.duration = lineAnimationDuration
        lineAnimator.start()

        lineAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) {
                dotAnimator.duration = dotAnimationDuration
                dotAnimator.start()
            }

            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
        })
    }

    fun getLineEnd(): PointF {
        return when (spotlightGravity) {
            SpotlightMessageGravity.right -> PointF(0f, height / 2f)
            SpotlightMessageGravity.left -> PointF(width.toFloat(), height / 2f)
            SpotlightMessageGravity.top -> PointF(width / 2f, height.toFloat())
            SpotlightMessageGravity.bottom -> PointF(width / 2f, 0f)
        }
    }

    fun getLineStart(): PointF {
        return when (spotlightGravity) {
            SpotlightMessageGravity.right -> PointF(width.toFloat(), height / 2f)
            SpotlightMessageGravity.left -> PointF(0f, height / 2f)
            SpotlightMessageGravity.top -> PointF(width / 2f, 0f)
            SpotlightMessageGravity.bottom -> PointF(width / 2f, height.toFloat())
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawLine(_start.x, _start.y, _end.x, _end.y, _linePaint)

        if (_dotInnerRadius > 0 || _dotOuterRadius > 0) {
            canvas?.drawCircle(_end.x, _end.y, _dotInnerRadius, _innerDotPaint)
            canvas?.drawCircle(_end.x, _end.y, _dotOuterRadius, _outerDotPaint)
        }
    }
}