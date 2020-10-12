package com.matadesigns.spotlight.abstraction

import android.graphics.Paint

interface SpotlightStyler {
    fun styleBackground(paint: Paint)
    fun styleTarget(paint: Paint)
}