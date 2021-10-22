package tw.firemaples.onscreenocr.floatings.floatingpoint

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.MovableFloatingView
import tw.firemaples.onscreenocr.utils.UIUtil

private const val TIME_THRESHOLD_FOR_LONG_CLICK = 1000L

private const val MSG_LONG_CLICKED = 1

class FloatingPoint(context: Context) : MovableFloatingView(context) {

    private val logger: Logger = LoggerFactory.getLogger(FloatingPoint::class.java)

    private val viewFloatingPoint: View = rootView.findViewById(R.id.view_floatingPoint)

    private var hasMoved: Boolean = false

    init {
        setViews()
        setDragView(viewFloatingPoint)
        setTouchInterceptor { v, event, hasMoved -> onTouch(v, event, hasMoved) }
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

    private val handler: Handler = Handler(Looper.getMainLooper()) {
        when (it.what) {
            MSG_LONG_CLICKED -> {
                logger.info("Long clicked, isNearTo: $isNearTo")
                return@Handler true
            }
        }
        return@Handler false
    }

    private fun onTouch(@Suppress("UNUSED_PARAMETER") v: View,
                        event: MotionEvent, hasMoved: Boolean): Boolean {
        this.hasMoved = hasMoved

        if (hasMoved) {
            handler.removeMessages(MSG_LONG_CLICKED)
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.sendMessageDelayed(
                        getMessage(MSG_LONG_CLICKED), TIME_THRESHOLD_FOR_LONG_CLICK)
            }

            MotionEvent.ACTION_UP -> {
                handler.removeMessages(MSG_LONG_CLICKED)
            }

            MotionEvent.ACTION_MOVE -> {

            }
        }

        return false
    }

    private fun getMessage(what: Int): Message = Message().apply { this.what = what }
}