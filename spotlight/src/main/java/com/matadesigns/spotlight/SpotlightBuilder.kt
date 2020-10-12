package com.matadesigns.spotlight

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.view.setPadding
import com.matadesigns.spotlight.abstraction.SpotlightListener
import com.matadesigns.spotlight.config.SpotlightDismissType
import com.matadesigns.spotlight.config.SpotlightMessageGravity

open class SpotlightBuilder(private var context: Context) {
    private var targetView: View? = null
    private var messageLayout: Int = R.layout.simple_message
    private var indicatorLayout: Int = R.layout.simple_indicator
    private var listener: SpotlightListener? = null
    private var dismissType: SpotlightDismissType = SpotlightDismissType.anywhere
    private var messageGravity: SpotlightMessageGravity? = null
    private var insetTop: Int = 0
    private var insetRight: Int = 0
    private var insetLeft: Int = 0
    private var insetBottom: Int = 0

    fun setInset(size: Int): SpotlightBuilder {
        val actualSize = (size * context.resources.displayMetrics.density).toInt()
        this.insetTop = actualSize
        this.insetBottom = actualSize
        this.insetLeft = actualSize
        this.insetRight = actualSize
        return this
    }

    fun setMessageGravity(messageGravity: SpotlightMessageGravity?): SpotlightBuilder {
        this.messageGravity = messageGravity
        return this;
    }

    fun setTargetView(view: View?): SpotlightBuilder {
        this.targetView = view;
        return this
    }

    fun setMessageLayout(@LayoutRes messageLayout: Int): SpotlightBuilder {
        this.messageLayout = messageLayout
        return this
    }

    fun setIndicatorLayout(@LayoutRes indicatorLayout: Int): SpotlightBuilder {
        this.indicatorLayout = indicatorLayout
        return this
    }

    fun setListener(listener: SpotlightListener?): SpotlightBuilder {
        this.listener = listener
        return this
    }

    fun setDismissType(dismissType: SpotlightDismissType): SpotlightBuilder {
        this.dismissType = dismissType
        return this
    }

    fun build(): SpotlightView {
        return SpotlightView(context).also {
            it.dismissType = this.dismissType
            it.listener = this.listener
            it.indicatorLayout = this.indicatorLayout
            it.messageLayout = this.messageLayout
            it.messageGravity = this.messageGravity
            it.targetView = this.targetView
            it.insetBottom = this.insetBottom
            it.insetTop = this.insetTop
            it.insetLeft = this.insetLeft
            it.insetRight = this.insetRight
        }
    }
}