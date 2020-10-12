package com.matadesigns.spotlight.abstraction

import android.graphics.Path
import android.graphics.RectF

interface SpotlightTarget {
    var spotlightPath: Path
    var boundingRect: RectF
}