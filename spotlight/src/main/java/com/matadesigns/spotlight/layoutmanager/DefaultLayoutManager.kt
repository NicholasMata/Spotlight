package com.matadesigns.spotlight.layoutmanager

import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.View
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.abstraction.SpotlightLayoutManager
import com.matadesigns.spotlight.config.SpotlightMessageGravity
import java.util.*

class DefaultLayoutManager : SpotlightLayoutManager {

    companion object {
        val TAG = "DefaultLayoutManager"
    }

    class IndicatorLayoutInfo(
        var marginStart: Int,
        var marginEnd: Int,
        var maxLength: Int
    ) {

        val totalLength: Int
            get() {
                return marginStart + maxLength + marginEnd
            }

        companion object {
            var default = IndicatorLayoutInfo(10, 0, 40)
        }
    }

    var indicatorLayoutInfo: IndicatorLayoutInfo = IndicatorLayoutInfo.default

    fun distanceBetween(view: View, rect: Rect, messageGravity: SpotlightMessageGravity): Int {
        return when (messageGravity) {
            SpotlightMessageGravity.bottom -> (view.bottom - getNavigationBarSize(view)) - rect.bottom
            SpotlightMessageGravity.left -> rect.left - view.left
            SpotlightMessageGravity.right -> view.right - rect.right
            SpotlightMessageGravity.top -> rect.top - view.top
        }
    }

    private fun getNavigationBarSize(root: View): Int {
        val resources = root.context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    override fun gravityFor(
        targetRect: Rect,
        message: View,
        root: SpotlightView
    ): SpotlightMessageGravity {
        val distancesMap = SpotlightMessageGravity.values()
            .fold(mutableMapOf<SpotlightMessageGravity, Int>(), { acc, spotlightMessageGravity ->
                val distance = distanceBetween(root, targetRect, spotlightMessageGravity)
                acc.put(spotlightMessageGravity, distance)
                acc
            })

        val greatestDistance = distancesMap.values.maxOrNull()
        val indexOfGreatest = distancesMap.values.indexOf(greatestDistance)

        return distancesMap.keys.elementAtOrElse(
            indexOfGreatest,
            { SpotlightMessageGravity.default })
    }

    override fun layoutViews(
        messageGravity: SpotlightMessageGravity,
        targetRect: Rect,
        indicator: View,
        message: View,
        root: SpotlightView
    ) {
        val rootRect = Rect(
            root.left + root.insetLeft,
            root.top + root.insetTop,
            root.right - root.insetRight,
            root.bottom - root.insetBottom
        )

        val density = root.context.resources.displayMetrics.density
        val totalDistanceAvailable = distanceBetween(root, targetRect, messageGravity)

        val messageHeight = message.height
        val messageWidth = message.width


        if (totalDistanceAvailable < messageWidth) {
            Log.w(
                TAG,
                "Message will not fit on ${messageGravity.name} either make a custom SpotlightLayoutManager or don't set messageGravity manually if you do."
            )
        }

        val marginStart = (indicatorLayoutInfo.marginStart * density).toInt()
        val marginEnd = (indicatorLayoutInfo.marginEnd * density).toInt()

        val distanceAvailableIfMargins = totalDistanceAvailable - (marginStart + marginEnd)

        val indicatorLength: Int
        val maxIndicatorLength = (indicatorLayoutInfo.maxLength * density).toInt()
        when (messageGravity) {
            SpotlightMessageGravity.right, SpotlightMessageGravity.left -> {
                val indicatorLengthAvailable = distanceAvailableIfMargins - messageWidth
                indicatorLength =
                    Math.max(Math.min(indicatorLengthAvailable, maxIndicatorLength), 0)
            }
            SpotlightMessageGravity.top, SpotlightMessageGravity.bottom -> {
                val indicatorLengthAvailable = distanceAvailableIfMargins - messageHeight
                indicatorLength =
                    Math.max(Math.min(indicatorLengthAvailable, maxIndicatorLength), 0)
            }
        }

        var actualMarginStart = marginStart
        var actualMarginEnd = marginEnd
        if (distanceAvailableIfMargins < 0 || indicatorLength == 0) {
            actualMarginStart = 0
            actualMarginEnd = 0
        }

        val targetCenterX = targetRect.centerX()
        val targetCenterY = targetRect.centerY()

        val messageRect: Rect
        val indicatorRect: Rect
        when (messageGravity) {
            SpotlightMessageGravity.bottom -> {
                /**
                 * --------------- Target Rect Top
                 * |+++++++++++++|
                 * |+++++++++++++|
                 * --------------- Target Rect Bottom
                 * ==================================== Indicator Margin Start
                 * --------------- Indicator Top
                 * |xxxxxxxxxxxxx|
                 * |xxxxxxxxxxxxx|
                 * --------------- Indicator Bottom
                 * ==================================== Indicator Margin End
                 * --------------- Message Top
                 * |#############|
                 * |#############|
                 * --------------- Message Bottom
                 */
                val indicatorLeft = targetRect.left
                val indicatorTop = targetRect.bottom + actualMarginStart
                val indicatorBottom = indicatorTop + indicatorLength
                indicatorRect = Rect(
                    indicatorLeft,
                    indicatorTop,
                    targetRect.right,
                    indicatorBottom
                )

                val messageLeft = Math.max(targetCenterX - (messageWidth / 2), 0)
                val messageTop = indicatorBottom + actualMarginEnd
                val messageRight = messageLeft + messageWidth
                val messageBottom = messageTop + messageHeight
                messageRect = Rect(
                    messageLeft,
                    messageTop,
                    messageRight,
                    messageBottom
                )
            }
            SpotlightMessageGravity.top -> {
                /**
                 * --------------- Message Top
                 * |#############|
                 * |#############|
                 * --------------- Message Bottom
                 * ==================================== Indicator Margin End
                 * --------------- Indicator Top
                 * |xxxxxxxxxxxxx|
                 * |xxxxxxxxxxxxx|
                 * --------------- Indicator Bottom
                 * ==================================== Indicator Margin Start
                 * --------------- Target Rect Top
                 * |+++++++++++++|
                 * |+++++++++++++|
                 * --------------- Target Rect Bottom
                 */
                val indicatorBottom = targetRect.top - actualMarginStart
                val indicatorLeft = targetRect.left
                val indicatorTop = indicatorBottom - indicatorLength
                indicatorRect = Rect(
                    indicatorLeft,
                    indicatorTop,
                    targetRect.right,
                    indicatorBottom
                )

                val messageBottom = indicatorTop - actualMarginEnd
                val messageLeft = targetCenterX - (messageWidth / 2)
                val messageRight = messageLeft + messageWidth
                val messageTop = messageBottom - messageHeight
                messageRect = Rect(
                    messageLeft,
                    messageTop,
                    messageRight,
                    messageBottom
                )
            }
            SpotlightMessageGravity.left -> {
                /**
                 * ML MR    IL IR    TL TR
                 * |--|  |  |--|  |  |--|
                 * |##|  |  |xx|  |  |xx|
                 * |##|  |  |xx|  |  |xx|
                 * |##|  |  |xx|  |  |xx|
                 * |##|  |  |xx|  |  |xx|
                 * |--|  |  |--|  |  |--|
                 */

                val indicatorRight = targetRect.left - actualMarginStart
                val indicatorLeft = indicatorRight - indicatorLength

                indicatorRect = Rect(
                    indicatorLeft,
                    targetRect.top,
                    indicatorRight,
                    targetRect.bottom
                )

                val messageRight = indicatorLeft - actualMarginEnd
                val messageLeft = messageRight - messageWidth
                val messageTop = targetCenterY - (messageHeight / 2)
                val messageBottom = messageTop + messageHeight
                messageRect = Rect(
                    messageLeft,
                    messageTop,
                    messageRight,
                    messageBottom
                )
            }
            SpotlightMessageGravity.right -> {
                /**
                 * TL TR    IL IR     ML MR
                 * |--|  |  |--|  |   |--|
                 * |++|  |  |xx|  |   |##|
                 * |++|  |  |xx|  |   |##|
                 * |++|  |  |xx|  |   |##|
                 * |++|  |  |xx|  |   |##|
                 * |--|  |  |--|  |   |--|
                 */

                val indicatorLeft = targetRect.right + actualMarginStart
                val indicatorRight = indicatorLeft + indicatorLength
                indicatorRect = Rect(
                    indicatorLeft,
                    targetRect.top,
                    indicatorRight,
                    targetRect.bottom
                )

                val messageLeft = indicatorRight + actualMarginEnd
                val messageRight = messageLeft + messageWidth
                val messageTop = targetCenterY - (messageHeight / 2)
                val messageBottom = messageTop + messageHeight
                messageRect = Rect(
                    messageLeft,
                    messageTop,
                    messageRight,
                    messageBottom
                )
            }
        }

        repositionInside(rootRect, messageRect)

        indicator.left = indicatorRect.left
        indicator.right = indicatorRect.right
        indicator.top = indicatorRect.top
        indicator.bottom = indicatorRect.bottom

        message.left = messageRect.left
        message.right = messageRect.right
        message.top = messageRect.top
        message.bottom = messageRect.bottom
    }

    private fun repositionInside(parent: Rect, child: Rect) {
        val width = child.width()
        val height = child.height()

        if (child.left < parent.left) {
            child.left = parent.left
            child.right = child.left + width
        }

        if (child.top < parent.top) {
            child.top = parent.top
            child.bottom = child.top + height
        }

        if (child.right > parent.right) {
            child.right = parent.right
            child.left = child.right - width
        }

        if (child.bottom > parent.bottom) {
            child.bottom = parent.bottom
            child.top = child.bottom - height
        }

    }
}