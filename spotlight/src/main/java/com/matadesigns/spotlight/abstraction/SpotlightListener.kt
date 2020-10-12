package com.matadesigns.spotlight.abstraction

import android.view.View

interface SpotlightListener {
    fun onEnd(targetView: View?)
    fun onStart(targetView: View?)
}