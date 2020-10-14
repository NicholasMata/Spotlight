package com.matadesigns.spotlight

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Spannable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.matadesigns.spotlight.abstraction.*
import com.matadesigns.spotlight.config.SpotlightDismissType
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlight.layoutmanager.DefaultLayoutManager
import com.matadesigns.spotlight.themes.simple.SimpleStyler

open class SpotlightView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected var _backgroundPaint = Paint()
    protected var _targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected var _targetRect = Rect()
    protected var _thisRect = Rect()

    protected var messageView: View
    protected var indicatorView: View

    public var insetTop: Int = 0
    public var insetBottom: Int = 0
    public var insetLeft: Int = 0
    public var insetRight: Int = 0

    @LayoutRes
    public var indicatorLayout: Int = R.layout.simple_indicator

    @LayoutRes
    public var messageLayout: Int = R.layout.simple_message

    public var styler: SpotlightStyler = SimpleStyler()
        set(value) {
            field = value
            postInvalidate()
        }

    public var layoutManager: SpotlightLayoutManager = DefaultLayoutManager()
    public var messageGravity: SpotlightMessageGravity? = null
    public var targetView: View? = null
        set(value) {
            field = value
        }
    public var listener: SpotlightListener? = null
    public var dismissType: SpotlightDismissType = SpotlightDismissType.targetView
    public var startAnimation: Animation = AlphaAnimation(0.0f, 1.0f).also {
        it.duration = 400;
        it.fillAfter = true
    }

    var startAnimationListener: Animation.AnimationListener

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)

        val inflater = LayoutInflater.from(context)
        indicatorView = inflater.inflate(indicatorLayout, this, false)
        messageView = inflater.inflate(messageLayout, this, false)
        messageView.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        indicatorView.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

        addView(indicatorView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
        addView(messageView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)

        startAnimationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                (this@SpotlightView.messageView as? SpotlightAnimatable)?.startAnimation()
                (this@SpotlightView.indicatorView as? SpotlightAnimatable)?.startAnimation()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        }
        startAnimation.setAnimationListener(startAnimationListener)
    }

    fun setInset(size: Int) {
        this.insetLeft = size
        this.insetRight = size
        this.insetTop = size
        this.insetBottom = size
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            _thisRect.set(
                left,
                top,
                right,
                bottom
            )
            layoutViews()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        _thisRect.set(
            _thisRect.left,
            _thisRect.top,
            _thisRect.left + w,
            _thisRect.top + h
        )
        postInvalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val targetView = targetView
        if (targetView != null) {
            styler.styleBackground(_backgroundPaint)
            canvas?.drawRect(_thisRect, _backgroundPaint)

            styler.styleTarget(_targetPaint)
            if (targetView is SpotlightTarget) {
                val targetPath = targetView.spotlightPath
                canvas?.drawPath(targetPath, _targetPaint)
            } else {
                canvas?.drawRect(_targetRect, _targetPaint)
            }
        }
    }

    public fun startSpotlight() {
        layoutViews()

        isClickable = false

        val context = this.context
        when (context) {
            is Activity -> {
                (context.window.decorView as? ViewGroup)?.addView(this)
            }
            is Fragment -> {
                (context.activity?.window?.decorView as? ViewGroup)?.addView(this)
            }
        }
        this.startAnimation(startAnimation)
        listener?.onStart(targetView)
    }

    public fun endSpotlight() {
        ((context as? Activity)?.window?.decorView as? ViewGroup)?.removeView(this)
        listener?.onEnd(targetView)
    }

    fun setTargetViewPosition() {
        val targetView = targetView
        if (targetView != null) {
            val locationTarget = IntArray(2)
            targetView.getLocationOnScreen(locationTarget)
            _targetRect.set(
                locationTarget[0],
                locationTarget[1],
                locationTarget[0] + targetView.width,
                locationTarget[1] + targetView.height
            )
            postInvalidate()
        } else {
            _targetRect.set(0, 0, 0, 0)
        }
    }

    protected fun layoutViews() {
        setTargetViewPosition()
        val messageGravity =
            this.messageGravity ?: layoutManager.gravityFor(
                _targetRect,
                messageView,
                _thisRect,
                this
            )
        (indicatorView as? SpotlightIndicator)?.spotlightGravity = messageGravity
        (indicatorView as? SpotlightMessage)?.spotlightGravity = messageGravity
        layoutManager.layoutViews(
            messageGravity,
            _targetRect,
            indicatorView,
            messageView,
            _thisRect,
            this
        )
    }

    override fun performClick(): Boolean {
        endSpotlight()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false;
        val x = event.x
        val y = event.y
        if (event.action == ACTION_DOWN) {
            when (dismissType) {
                SpotlightDismissType.outside -> {
                    if (!viewContains(messageView, x, y)) {
                        performClick()
                    }
                }
                SpotlightDismissType.targetView -> {
                    if (viewContains(targetView, x, y)) {
                        performClick()
                    }
                }
                SpotlightDismissType.messageView -> {
                    if (viewContains(messageView, x, y)) {
                        performClick()
                    }
                }
                SpotlightDismissType.anywhere -> {
                    performClick()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun viewContains(view: View?, rx: Float, ry: Float): Boolean {
        if (view == null) return false;
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        val w = view.width
        val h = view.height
        return !(rx < x || rx > x + w || ry < y || ry > y + h)
    }

    public fun setTitle(text: CharSequence) {
        val titleView = messageView.findViewById<View>(R.id.spotlight_title)
        if (titleView is TextView) {
            titleView.text = text
        }
    }

    public fun setDescription(text: CharSequence) {
        val descriptionView = messageView.findViewById<View>(R.id.spotlight_description)
        if (descriptionView is TextView) {
            descriptionView.text = text
        }
    }
}