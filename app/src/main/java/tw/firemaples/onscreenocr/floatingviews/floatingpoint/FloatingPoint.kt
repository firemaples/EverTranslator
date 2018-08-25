package tw.firemaples.onscreenocr.floatingviews.floatingpoint

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.MovableFloatingView
import tw.firemaples.onscreenocr.utils.UIUtil

class FloatingPoint(context: Context) : MovableFloatingView(context) {
    val viewFloatingPoint: View = rootView.findViewById(R.id.view_floatingPoint)

    init {
        setViews()
        setDragView(viewFloatingPoint)
    }

    override fun getLayoutId(): Int =
            R.layout.view_floating_point

    @SuppressLint("RtlHardcoded")
    override fun getLayoutGravity(): Int = Gravity.TOP or Gravity.RIGHT

    override fun getLayoutSize(): Int {
        return UIUtil.dpToPx(context, 40f)
    }

    override fun enableAutoMoveToEdge(): Boolean = true

    override fun enableTransparentWhenMoved(): Boolean = true

    private fun setViews() {

    }
}