package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import tw.firemaples.onscreenocr.CoreApplication

/**
 * Created by louis1chen on 26/03/2018.
 */

object UIUtil {
    private val context by lazy { CoreApplication.instance }

    private val displayMetrics by lazy {
        val metrics = DisplayMetrics()
        (CoreApplication.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getMetrics(metrics)
        metrics
    }

    fun getScreenSize(): IntArray {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val size = Point()
        display.getRealSize(size)
        val mWidth = size.x
        val mHeight = size.y
//        val mDensity = metrics.densityDpi
        val isPortrait = mHeight > mWidth

        var deviceWidth = metrics.widthPixels
        var deviceHeight = metrics.heightPixels
        if (deviceHeight > deviceWidth != isPortrait) {
            deviceWidth = metrics.heightPixels

            deviceHeight = metrics.widthPixels
        }

        return intArrayOf(deviceWidth, deviceHeight)
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    fun spToPx(context: Context, sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
    }

    fun countViewPosition(anchorRect: Rect, itemWidth: Int, itemHeight: Int, layoutMargin: Int,
                          parentHeight: Int = UIUtil.getScreenSize()[1]): Array<Int> {

        val needHeight = itemHeight + layoutMargin * 2

        val topMargin: Int = when {
            parentHeight - anchorRect.bottom > needHeight -> //Gravity = BOTTOM
                anchorRect.top + anchorRect.height()
            anchorRect.top > needHeight -> //Gravity = TOP
                anchorRect.top - itemHeight
            else -> -1
        }

        val leftMargin: Int = if (anchorRect.left + itemWidth + layoutMargin > UIUtil.getScreenSize()[0]) {
            // Match screen right
            UIUtil.getScreenSize()[0] - (itemWidth - layoutMargin)
        } else {
            // Match anchorView left
            anchorRect.left
        }

        return arrayOf(leftMargin, topMargin)
    }
}
