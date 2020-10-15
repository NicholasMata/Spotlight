package com.matadesigns.spotlight.abstraction

import android.graphics.Path
import android.graphics.RectF
import android.view.MotionEvent

interface SpotlightTarget {
    val spotlightPath: Path
    val boundingRect: RectF
    val handlesSpotlightTouchEvent: Boolean

    fun onSpotlightTouchEvent(event: MotionEvent): Boolean
}