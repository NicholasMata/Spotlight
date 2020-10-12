package com.matadesigns.spotlight.themes.simple

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import com.matadesigns.spotlight.abstraction.SpotlightStyler

class SimpleStyler : SpotlightStyler {
    var xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    override fun styleBackground(paint: Paint) {
        paint.setColor(-0x67000000)
        paint.setStyle(Paint.Style.FILL)
        paint.setAntiAlias(true)
    }

    override fun styleTarget(paint: Paint) {
        paint.isAntiAlias = true
        paint.xfermode = xfermode
    }
}