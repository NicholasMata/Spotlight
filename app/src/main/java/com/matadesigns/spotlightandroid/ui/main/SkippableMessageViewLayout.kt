package com.matadesigns.spotlightandroid.ui.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.abstraction.SpotlightMessage
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import com.matadesigns.spotlightandroid.R
import kotlinx.android.synthetic.main.spotlight_skippable_message_view.view.*

class SkippableMessageViewLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), SpotlightMessage {
    override var spotlightGravity: SpotlightMessageGravity? = null
    override var spotlightView: SpotlightView? = null


    override fun onFinishInflate() {
        super.onFinishInflate()

        val button = findViewById<MaterialButton>(R.id.spotlight_message_next_button)
        button.setOnClickListener {
            spotlightView?.endSpotlight()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            button.visibility = View.VISIBLE
        }, 5000)
    }
}