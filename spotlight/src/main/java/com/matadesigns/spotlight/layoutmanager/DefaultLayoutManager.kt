package com.matadesigns.spotlight.layoutmanager

import android.graphics.Rect
import android.util.Log
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import com.matadesigns.spotlight.SpotlightView
import com.matadesigns.spotlight.abstraction.SpotlightLayoutManager
import com.matadesigns.spotlight.config.SpotlightMessageGravity


class DefaultLayoutManager : SpotlightLayoutManager {

    companion object {
        val TAG = "DefaultLayoutManager"
    }

    class IndicatorLayoutInfo(
        var marginStart: Int,
        var marginEnd: Int,
        var maxLength: Int,
        var minLength: Int
    ) {
        companion object {
            var default = IndicatorLayoutInfo(
                10,
                0,
                40,
                20
            )
        }
    }

    class MessageLayoutInfo(
        var resizable: Boolean
    ) {
        companion object {
            var default = MessageLayoutInfo(true)
        }
    }

    var messageLayoutInfo: MessageLayoutInfo = MessageLayoutInfo.default
    var indicatorLayoutInfo: IndicatorLayoutInfo = IndicatorLayoutInfo.default

    fun distanceBetween(
        view: View,
        parent: Rect,
        rect: Rect,
        messageGravity: SpotlightMessageGravity
    ): Int {
        val availableArea = availableRect(view, parent, rect, messageGravity)
        return when(messageGravity) {
            SpotlightMessageGravity.right, SpotlightMessageGravity.left -> availableArea.width
            SpotlightMessageGravity.top, SpotlightMessageGravity.bottom -> availableArea.height
        }
    }

    fun availableRect(
        view: View,
        parent: Rect,
        rect: Rect,
        messageGravity: SpotlightMessageGravity
    ): Size {
        val heightMinusNavigation = parent.bottom - getNavigationBarSize(view)
        val width = parent.width()
        return when (messageGravity) {
            SpotlightMessageGravity.bottom -> Size(
                width,
                heightMinusNavigation - rect.bottom
            )
            SpotlightMessageGravity.left -> Size(
                rect.left - parent.left,
                heightMinusNavigation
            )
            SpotlightMessageGravity.right -> Size(
                parent.right - rect.right,
                heightMinusNavigation
            )
            SpotlightMessageGravity.top -> Size(
                width,
                rect.top - parent.top
            )
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
        parentRect: Rect,
        root: SpotlightView
    ): SpotlightMessageGravity {
        val distancesMap = SpotlightMessageGravity.values()
            .fold(mutableMapOf<SpotlightMessageGravity, Int>(), { acc, spotlightMessageGravity ->
                val distance = distanceBetween(
                    root,
                    parentRect,
                    targetRect,
                    spotlightMessageGravity
                )
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
        parentRect: Rect,
        root: SpotlightView
    ) {
        val rootRect = Rect(
            parentRect.left + root.insetLeft,
            parentRect.top + root.insetTop,
            parentRect.right - root.insetRight,
            parentRect.bottom - root.insetBottom
        )

        if (rootRect.width() == 0 && rootRect.height() == 0) return

        val density = root.context.resources.displayMetrics.density
        val totalDistanceAvailable = distanceBetween(root, rootRect, targetRect, messageGravity)


        val marginStart = (indicatorLayoutInfo.marginStart * density).toInt()
        val marginEnd = (indicatorLayoutInfo.marginEnd * density).toInt()
        val minIndicatorLength = (indicatorLayoutInfo.minLength * density).toInt()

        val originalMessageHeight = message.height
        val originalMessageWidth = message.width

        val messageHeight: Int
        val messageWidth: Int

        if (messageLayoutInfo.resizable) {
            val totalAreaAvailable = availableRect(root, rootRect, targetRect, messageGravity)
            val fullMinIndicatorLength = minIndicatorLength + marginStart + marginEnd
            var width = totalAreaAvailable.width
            var height = totalAreaAvailable.height
            when (messageGravity) {
                SpotlightMessageGravity.bottom, SpotlightMessageGravity.top -> {
                    height -= fullMinIndicatorLength
                }
                SpotlightMessageGravity.left, SpotlightMessageGravity.right -> {
                    width -= fullMinIndicatorLength
                }
            }
            val widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
            val heightMeasureSpec =
                MeasureSpec.makeMeasureSpec(totalAreaAvailable.height, MeasureSpec.AT_MOST)
            message.measure(widthMeasureSpec, heightMeasureSpec)
            messageWidth = message.measuredWidth
            messageHeight = message.measuredHeight
            if (originalMessageHeight != messageHeight && originalMessageWidth != messageWidth) {
                val layoutParams = message.layoutParams
                layoutParams.width = messageWidth
                layoutParams.height = messageHeight
                message.layoutParams = layoutParams
            }
        } else {
            messageHeight = originalMessageHeight
            messageWidth = originalMessageWidth
        }

        if (totalDistanceAvailable < messageWidth) {
            Log.w(
                TAG,
                "Message will not fit on ${messageGravity.name} either make a custom SpotlightLayoutManager or don't set messageGravity manually if you do."
            )
        }

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