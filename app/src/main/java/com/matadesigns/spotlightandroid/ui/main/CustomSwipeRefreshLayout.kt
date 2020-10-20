package com.matadesigns.spotlightandroid.ui.main

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.abstraction.SpotlightTarget
import com.matadesigns.spotlight.utils.SpotlightMath

class CustomSwipeRefreshLayout: SwipeRefreshLayout, SpotlightTarget {

    constructor(context: Context) : super(context){
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    {
    }

    override var spotlightView: SpotlightView? = null

    override val spotlightPath: Path
        get() {
            val path = Path()
            path.addRect(boundingRect, Path.Direction.CW)
            return path
        }
    override val boundingRect: RectF
        get() {
            return SpotlightMath.rectFOnScreen(this)
        }
    override val handlesSpotlightTouchEvent: Boolean = true

    override fun onSpotlightTouchEvent(event: MotionEvent): Boolean {
        return !boundingRect.contains(event.x, event.y)
    }
}