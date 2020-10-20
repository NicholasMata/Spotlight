package com.matadesigns.spotlight.abstraction

import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent
import com.matadesigns.spotlight.SpotlightView

interface SpotlightTarget {
    val spotlightPath: Path
    val boundingRect: RectF
    val handlesSpotlightTouchEvent: Boolean
    var spotlightView: SpotlightView?

    fun onSpotlightTouchEvent(event: MotionEvent): Boolean
}