package com.matadesigns.spotlight.utils

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRectF

class SpotlightMath {
    companion object {
        /**
         * Get the top left position for the given view relative to the screen.
         *
         * @param view The view you want the point on the screen for.
         * @return The top left position of the view as a point on the screen
         */
        fun pointOnScreen(view: View): Point {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return Point(location[0], location[1])
        }

        /**
         * Get the top left position for the given view relative to the screen.
         *
         * @param view The view you want the point on the screen for.
         * @return The top left position of the view as a point on the screen
         */
        fun pointFOnScreen(view: View): PointF {
            return pointOnScreen(view).toPointF()
        }

        /**
         * Get the view's rect relative to the screen.
         *
         * @param view The view you want the rect on the screen for.
         * @return The rect for the view relative to the screen.
         */
        fun rectOnScreen(view: View): Rect {
            val screenPoint = pointOnScreen(view)

            return Rect(
                screenPoint.x,
                screenPoint.y,
                screenPoint.x + view.width,
                screenPoint.y + view.height
            )
        }

        /**
         * Get the view's rect relative to the screen.
         *
         * @param view The view you want the rect on the screen for.
         * @return The rect for the view relative to the screen.
         */
        fun rectFOnScreen(view: View): RectF {
            return rectOnScreen(view).toRectF()
        }
    }
}