package com.matadesigns.spotlight.utils

import android.os.SystemClock
import android.util.Log
import android.view.View

class DebounceOnClickListener(var debounceTime: Long, var action: (View) -> Unit): View.OnClickListener {
    private var clickTime: Long = 0

    override fun onClick(v: View) {
        val lastClickTime = clickTime
        clickTime = SystemClock.elapsedRealtime()
        if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) {
            Log.d("DebounceOnClickListener", "Blocked Click")
            return
        }
        action(v)
    }
}

fun View.setOnClickListenerWithDebounce(action: (View) -> Unit, debounceTime: Long = 600L) {
    this.setOnClickListener(DebounceOnClickListener(debounceTime, action))
}