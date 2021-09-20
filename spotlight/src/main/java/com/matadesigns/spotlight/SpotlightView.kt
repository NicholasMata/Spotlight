package com.matadesigns.spotlight

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.graphics.toRect
import androidx.core.graphics.toRectF
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.matadesigns.spotlight.abstraction.*
import com.matadesigns.spotlight.config.SpotlightDismissType
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlight.layoutmanager.DefaultLayoutManager
import com.matadesigns.spotlight.themes.simple.SimpleStyler
import com.matadesigns.spotlight.utils.SpotlightMath

open class SpotlightView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var backgroundPaint = Paint()
        protected set
    var targetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        protected set
    var targetRect = Rect()
        protected set
    var thisRect = Rect()
        protected set

    var messageView: View
        protected set
    var indicatorView: View
        protected set

    var insetTop: Int = 0
    var insetBottom: Int = 0
    var insetLeft: Int = 0
    var insetRight: Int = 0

    var title: CharSequence? = null
        get() {
            return field
        }
        set(value) {
            field = value
            applyTitle(value)
        }

    var description: CharSequence? = null
        get() {
            return field
        }
        set(value) {
            field = value
            applyDescription(value)
        }


    @LayoutRes
    var indicatorLayout: Int = R.layout.simple_indicator
        set(value) {
            if (field != value) {
                field = value
                removeView(indicatorView)
                val inflater = LayoutInflater.from(context)
                indicatorView = inflater.inflate(value, this, false)
                addView(indicatorView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
            }
        }

    @LayoutRes
    var messageLayout: Int = R.layout.simple_message
        set(value) {
            if (field != value) {
                field = value
                (messageView as? SpotlightMessage)?.spotlightView = this
                removeView(messageView)
                val inflater = LayoutInflater.from(context)
                messageView = inflater.inflate(value, this, false)
                applyTitle(title)
                applyDescription(description)
                addView(messageView, LayoutParams(WRAP_CONTENT, WRAP_CONTENT))
                (messageView as? SpotlightMessage)?.spotlightView = this
            }
        }

    var styler: SpotlightStyler = SimpleStyler(context)
        set(value) {
            field = value
            postInvalidate()
        }

    var layoutManager: SpotlightLayoutManager = DefaultLayoutManager()
    var messageGravity: SpotlightMessageGravity? = null
    var targetView: View? = null
        set(value) {
            field = value
            (value as? SpotlightTarget)?.spotlightView = this
        }
    var listener: SpotlightListener? = null
    var dismissType: SpotlightDismissType = SpotlightDismissType.targetView
    var passThrough = false
    var startAnimation: Animation = AlphaAnimation(0.0f, 1.0f).also {
        it.duration = 400
        it.fillAfter = true
    }

    private var startAnimationListener: Animation.AnimationListener

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

        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT).also {
            if (context.resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                it.bottomMargin = getNavigationBarSize(this)
            }
        }

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

    override fun onFinishInflate() {
        super.onFinishInflate()

        layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun getNavigationBarSize(root: View): Int {
        val resources = root.context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun setInset(size: Int) {
        this.insetLeft = size
        this.insetRight = size
        this.insetTop = size
        this.insetBottom = size
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        thisRect.set(
            left,
            top,
            right,
            bottom
        )
        layoutViews()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val targetView = targetView
        if (targetView != null) {
            styler.styleBackground(backgroundPaint)
            canvas?.drawRect(thisRect, backgroundPaint)

            styler.styleTarget(targetPaint)
            if (targetView is SpotlightTarget) {
                val targetPath = targetView.spotlightPath
                canvas?.drawPath(targetPath, targetPaint)
            } else {
                canvas?.drawRect(targetRect, targetPaint)
            }
        }
    }

    fun startSpotlight(viewGroup: ViewGroup? = null, animate: Boolean = true) {
        layoutViews()

        if (viewGroup != null) {
            viewGroup.addView(this)
        } else {
            val context = this.context
            when (context) {
                is Activity -> {
                    (context.window.decorView as? ViewGroup)?.addView(this)
                }
                is DialogFragment -> {
                    (context.dialog?.window?.decorView as? ViewGroup)?.addView(this)
                }
                is Fragment -> {
                    (context.activity?.window?.decorView as? ViewGroup)?.addView(this)
                }
            }
        }
        if (!animate) {
            startAnimation.duration = 0
        }
        this.startAnimation(startAnimation)
        listener?.onStart(targetView)
    }

    fun endSpotlight() {
        listener?.onEnd(targetView)
        (messageView as? SpotlightMessage)?.spotlightView = null
        (targetView as? SpotlightTarget)?.spotlightView = null
        (parent as? ViewGroup)?.removeView(this)
    }

    fun setTargetViewPosition() {
        val targetView = targetView
        if (targetView != null) {
            if (targetView is SpotlightTarget) {
                val boundingRect = targetView.boundingRect.toRect()
                targetRect.set(boundingRect)
            } else {
                val point = SpotlightMath.pointOnScreen(targetView)
                val left = point.x
                val top = point.y
                targetRect.set(
                    left,
                    top,
                    left + targetView.width,
                    top + targetView.height
                )
            }
            postInvalidate()
        } else {
            targetRect.set(0, 0, 0, 0)
        }
    }

    protected fun layoutViews() {
        setTargetViewPosition()
        val messageGravity =
            this.messageGravity ?: layoutManager.gravityFor(
                targetRect,
                messageView,
                thisRect,
                this
            )
        (indicatorView as? SpotlightIndicator)?.spotlightGravity = messageGravity
        (indicatorView as? SpotlightMessage)?.spotlightGravity = messageGravity
        layoutManager.layoutViews(
            messageGravity,
            targetRect,
            indicatorView,
            messageView,
            thisRect,
            this
        )
    }

    override fun performClick(): Boolean {
        endSpotlight()
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return false

        if (passThrough && dismissType == SpotlightDismissType.targetView) {
            val isInsideTarget = targetRect.toRectF().contains(event.x, event.y)
            return !isInsideTarget
        }

        val x = event.x
        val y = event.y
        val targetView = targetView
        if (dismissType == SpotlightDismissType.targetView &&
            targetView is SpotlightTarget && targetView.handlesSpotlightTouchEvent
        ) {
            return targetView.onSpotlightTouchEvent(event)
        }
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
        if (view == null) return false
        val point = SpotlightMath.pointOnScreen(view)
        val w = view.width
        val h = view.height
        return !(rx < point.x || rx > point.x + w || ry < point.y || ry > point.y + h)
    }

    protected fun applyTitle(text: CharSequence?) {
        val titleView = messageView.findViewById<View>(R.id.spotlight_title)
        if (titleView is TextView) {
            titleView.text = text
        }
    }

    protected fun applyDescription(text: CharSequence?) {
        val descriptionView = messageView.findViewById<View>(R.id.spotlight_description)
        if (descriptionView is TextView) {
            descriptionView.text = text
        }
    }
}