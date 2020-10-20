package com.matadesigns.spotlight

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import com.matadesigns.spotlight.abstraction.SpotlightListener
import com.matadesigns.spotlight.abstraction.SpotlightStyler
import com.matadesigns.spotlight.config.SpotlightDismissType
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlight.themes.simple.SimpleStyler

open class SpotlightBuilder(private var context: Context) {
    private var _targetView: View? = null
    private var _messageLayout: Int = R.layout.simple_message
    private var _indicatorLayout: Int = R.layout.simple_indicator
    private var _listener: SpotlightListener? = null
    private var _dismissType: SpotlightDismissType = SpotlightDismissType.anywhere
    private var _messageGravity: SpotlightMessageGravity? = null
    private var _insetTop: Int = 0
    private var _insetRight: Int = 0
    private var _insetLeft: Int = 0
    private var _insetBottom: Int = 0
    private var _title: CharSequence = ""
    private var _description: CharSequence = ""
    private var _styler: SpotlightStyler = SimpleStyler(context)

    fun setTitle(text: CharSequence): SpotlightBuilder {
        _title = text
        return this
    }

    fun setDescription(text: CharSequence): SpotlightBuilder {
        _description = text
        return this
    }

    fun setStyler(styler: SpotlightStyler): SpotlightBuilder {
        _styler = styler
        return this
    }

    fun setInset(size: Int): SpotlightBuilder {
        val actualSize = (size * context.resources.displayMetrics.density).toInt()
        this._insetTop = actualSize
        this._insetBottom = actualSize
        this._insetLeft = actualSize
        this._insetRight = actualSize
        return this
    }

    fun setMessageGravity(messageGravity: SpotlightMessageGravity?): SpotlightBuilder {
        this._messageGravity = messageGravity
        return this
    }

    fun setTargetView(view: View?): SpotlightBuilder {
        this._targetView = view
        return this
    }

    fun setMessageLayout(@LayoutRes messageLayout: Int): SpotlightBuilder {
        this._messageLayout = messageLayout
        return this
    }

    fun setIndicatorLayout(@LayoutRes indicatorLayout: Int): SpotlightBuilder {
        this._indicatorLayout = indicatorLayout
        return this
    }

    fun setListener(listener: SpotlightListener?): SpotlightBuilder {
        this._listener = listener
        return this
    }

    fun setDismissType(dismissType: SpotlightDismissType): SpotlightBuilder {
        this._dismissType = dismissType
        return this
    }

    fun build(): SpotlightView {
        return SpotlightView(context).also {
            it.dismissType = this._dismissType
            it.styler = this._styler
            it.listener = this._listener
            it.indicatorLayout = this._indicatorLayout
            it.messageLayout = this._messageLayout
            it.messageGravity = this._messageGravity
            it.targetView = this._targetView
            it.insetBottom = this._insetBottom
            it.insetTop = this._insetTop
            it.insetLeft = this._insetLeft
            it.insetRight = this._insetRight
            it.title = this._title
            it.description = this._description
        }
    }
}