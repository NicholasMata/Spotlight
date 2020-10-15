package com.matadesigns.spotlight.themes.simple

import android.content.Context
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Xfermode
import android.util.TypedValue
import androidx.core.graphics.ColorUtils
import com.matadesigns.spotlight.abstraction.SpotlightStyler

class SimpleStyler(protected var context: Context) : SpotlightStyler {
    var xfermode: Xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    override fun styleBackground(paint: Paint) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorForeground, typedValue, true)
        val color = typedValue.data

        paint.setColor(ColorUtils.setAlphaComponent(color, (255 * 0.7).toInt()))
        paint.setStyle(Paint.Style.FILL)
        paint.setAntiAlias(true)
    }

    override fun styleTarget(paint: Paint) {
        paint.isAntiAlias = true
        paint.xfermode = xfermode
    }
}