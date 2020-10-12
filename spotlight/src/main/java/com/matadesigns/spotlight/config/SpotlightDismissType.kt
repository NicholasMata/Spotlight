package com.matadesigns.spotlight.config

enum class SpotlightDismissType {
    /**
     * Dismiss the spotlight when anywhere but the message view is tapped
     */
    outside,

    /**
     * Dimiss the spotlight when the target view is tapped.
     */
    targetView,

    /**
     * Dismiss when anywhere is tapped.
     */
    anywhere,

    /**
     * Dismiss when message view is tapped.
     */
    messageView
}