package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.SparseIntArray
import android.util.TypedValue
import android.view.Surface
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object UIUtils {
    private val context by lazy { Utils.context }
    private val windowManager: WindowManager by lazy {
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private val orientations = SparseIntArray().apply {
        append(Surface.ROTATION_0, 90)
        append(Surface.ROTATION_90, 0)
        append(Surface.ROTATION_180, 270)
        append(Surface.ROTATION_270, 180)
    }

    val displayMetrics: DisplayMetrics
        get() = DisplayMetrics().also { windowManager.defaultDisplay.getMetrics(it) }

    private val realDisplayMetrics: DisplayMetrics
        get() = DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }

    private val orientation: Int
        get() = windowManager.defaultDisplay.orientation

    val orientationDegree: Int
        get() = orientations.get(orientation + 90)

    val realSize: Point
        get() = Point().also { windowManager.defaultDisplay.getRealSize(it) }

    private val isPortrait: Boolean
        get() = realSize.let { it.y > it.x }

    val screenSize: IntArray
        get() {
            val metrics = displayMetrics

            var deviceWidth = metrics.widthPixels
            var deviceHeight = metrics.heightPixels
            if (deviceHeight > deviceWidth != isPortrait) {
                deviceWidth = metrics.heightPixels

                deviceHeight = metrics.widthPixels
            }

            return intArrayOf(deviceWidth, deviceHeight)
        }

    val isStatusBarTakingVerticalSpace: Boolean
        get() = realDisplayMetrics.heightPixels != displayMetrics.heightPixels

    val isStatusBarTakingHorizontalSpace: Boolean
        get() = realDisplayMetrics.widthPixels != displayMetrics.widthPixels

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
        anchorRect: Rect, parentRect: Rect,
        itemWidth: Int, itemHeight: Int,
        layoutMargin: Int,
    ): Pair<Int, Int> {

        val parentHeight = parentRect.height()
        val needHeight = itemHeight + layoutMargin * 2 + parentRect.top

        val topMargin: Int = when {
            // anchor on bottom
            parentHeight - anchorRect.bottom > needHeight -> //Gravity = BOTTOM
                anchorRect.bottom + layoutMargin

            // anchor on top
            anchorRect.top > needHeight ->
                anchorRect.top - itemHeight - layoutMargin

            // center in vertical
            else -> (parentHeight - itemHeight) / 2
        } - parentRect.top

        val parentWidth = parentRect.width()
        val needWidth = itemWidth + layoutMargin + parentRect.left

        val leftMargin: Int =
            when {
                // start from the left of the anchor
                anchorRect.left + needWidth < parentWidth ->
                    anchorRect.left

                // end on the right of the anchor
                anchorRect.right - needWidth > 0 ->
                    anchorRect.right - needWidth

                // center in horizontal
                else -> (parentWidth - itemWidth) / 2
            } - parentRect.left

        return leftMargin to topMargin
    }
}

fun Number.dpToPx(): Int = UIUtils.dpToPx(this.toFloat())

fun View.onViewPrepared(callback: (View) -> Unit) {
    val view = this
    this.viewTreeObserver.addOnGlobalLayoutListener(object :
        ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (view.width == 0 || view.height == 0) {
                return
            }
            view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback(view)
        }
    })
}

fun View.getViewRect(): Rect {
    val location = IntArray(2).also { getLocationOnScreen(it) }
    return Rect(location[0], location[1], location[0] + measuredWidth, location[1] + measuredHeight)
}

fun TextView.setTextOrGone(text: String?) {
    if (text.isNullOrBlank()) this.visibility = View.GONE
    else {
        this.text = text
        this.visibility = View.VISIBLE
    }
}

fun View.show() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
}

fun View.hide() {
    if (visibility != View.GONE) {
        visibility = View.GONE
    }
}

fun View.showOrHide(show: Boolean) {
    if (show) show() else hide()
}

fun View.clickOnce(threshold: Long = 500L, action: () -> Unit) {
    setOnClickListener {
        isEnabled = false
        action.invoke()
        postDelayed({
            isEnabled = true
        }, threshold)
    }
}

fun View.showKeyboard() =
    ViewCompat.getWindowInsetsController(this)
        ?.show(WindowInsetsCompat.Type.ime())

fun View.hideKeyboard() =
    ViewCompat.getWindowInsetsController(this)
        ?.hide(WindowInsetsCompat.Type.ime())
