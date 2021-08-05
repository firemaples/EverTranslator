package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager

object UIUtils {
    private val context by lazy { Utils.context }
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    val displayMetrics: DisplayMetrics
        get() = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }

    val screenSize: IntArray
        get() {
            val metrics = displayMetrics
            val size = Point().also {
                windowManager.defaultDisplay.getRealSize(it)
            }

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

    fun dpToPx(dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun spToPx(sp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun countViewPosition(
        anchorRect: Rect, itemWidth: Int, itemHeight: Int, layoutMargin: Int,
        parentHeight: Int = screenSize[1]
    ): Array<Int> {

        val needHeight = itemHeight + layoutMargin * 2

        val topMargin: Int = when {
            parentHeight - anchorRect.bottom > needHeight -> //Gravity = BOTTOM
                anchorRect.top + anchorRect.height()
            anchorRect.top > needHeight -> //Gravity = TOP
                anchorRect.top - itemHeight
            else -> -1
        }

        val screenSize = screenSize

        val leftMargin: Int =
            if (anchorRect.left + itemWidth + layoutMargin > screenSize[0]) {
                // Match screen right
                screenSize[0] - (itemWidth - layoutMargin)
            } else {
                // Match anchorView left
                anchorRect.left
            }

        return arrayOf(leftMargin, topMargin)
    }
}

fun Float.dpToPx(): Int = UIUtils.dpToPx(this)
