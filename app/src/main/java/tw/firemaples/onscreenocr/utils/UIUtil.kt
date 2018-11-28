package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import tw.firemaples.onscreenocr.CoreApplication

/**
 * Created by louis1chen on 26/03/2018.
 */

object UIUtil {
    private val displayMetrics by lazy {
        val metrics = DisplayMetrics()
        (CoreApplication.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getMetrics(metrics)
        metrics
    }

    fun getScreenWidth(): Int = displayMetrics.widthPixels

    fun getScreenHeight(): Int = displayMetrics.heightPixels

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics).toInt()
    }

    fun spToPx(context: Context, sp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.resources.displayMetrics).toInt()
    }

    fun countViewPosition(anchorRect: Rect, itemWidth: Int, itemHeight: Int, layoutMargin: Int,
                          parentHeight: Int = displayMetrics.widthPixels): Array<Int> {

        val needHeight = itemHeight + layoutMargin * 2

        val topMargin: Int = when {
            parentHeight - anchorRect.bottom > needHeight -> //Gravity = BOTTOM
                anchorRect.top + anchorRect.height()
            anchorRect.top > needHeight -> //Gravity = TOP
                anchorRect.top - itemHeight
            else -> -1
        }

        val leftMargin: Int = if (anchorRect.left + itemWidth + layoutMargin > displayMetrics.widthPixels) {
            // Match screen right
            displayMetrics.widthPixels - (itemWidth - layoutMargin)
        } else {
            // Match anchorView left
            anchorRect.left
        }

        return arrayOf(leftMargin, topMargin)
    }
}
