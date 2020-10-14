package com.matadesigns.spotlight.abstraction

import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.config.SpotlightMessageGravity

interface SpotlightLayoutManager {
    fun gravityFor(
        targetRect: Rect,
        message: View,
        parentRect: Rect,
        root: SpotlightView
    ): SpotlightMessageGravity

    fun layoutViews(
        messageGravity: SpotlightMessageGravity,
        targetRect: Rect,
        indicator: View,
        message: View,
        parentRect: Rect,
        root: SpotlightView
    )
}