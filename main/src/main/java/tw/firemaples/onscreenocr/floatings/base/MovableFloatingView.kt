package tw.firemaples.onscreenocr.floatings.base

import android.animation.ValueAnimator
import android.content.Context
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.core.animation.addListener
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.utils.dpToPx
import kotlin.math.absoluteValue

abstract class MovableFloatingView(context: Context) : FloatingView(context) {
    companion object {
        private const val movedThreshold: Float = 20f

        private const val moveToEdgeDuration: Long = 450

        private const val fromAlpha: Float = 1f
        private const val fadeOutAnimationDuration: Long = 800
    }

    private val logger: Logger by lazy { Logger(this::class) }

    open val moveToEdgeAfterMoved: Boolean = false
    open val moveToEdgeMarginInDP: Float = 0f
    private val moveToEdgeMargin: Int by lazy { moveToEdgeMarginInDP.dpToPx() }
    override val layoutCanMoveOutsideScreen: Boolean
        get() = moveToEdgeAfterMoved

    open val fadeOutAfterMoved: Boolean = false
    open val fadeOutDelay: Long = 1000L
    open val fadeOutDestinationAlpha: Float = 0.2f

    private lateinit var dragView: View

    fun setDragView(view: View) {
        with(view) {
            dragView = view
            isClickable = true
            isFocusable = true
            setOnTouchListener(onTouchListener)
        }
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        moveToEdgeOrFadeOut()
    }

    private var initX: Int = 0
    private var initY: Int = 0
    private var initTouchX: Float = 0f
    private var initTouchY: Float = 0f
    private var hasMoved: Boolean = false

    protected var touchInterceptor: ((view: View, motionEvent: MotionEvent, hasMoved: Boolean) -> Boolean)? =
        null

    private val onTouchListener: (View, MotionEvent) -> Boolean =
        TouchListener@{ view, event ->
            cancelFadeOut()

            if (touchInterceptor?.invoke(view, event, hasMoved) == true)
                return@TouchListener true

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    hasMoved = false
                    initX = params.x
                    initY = params.y
                    initTouchX = event.rawX
                    initTouchY = event.rawY
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_CANCEL -> {
                    val moved = hasMoved
                    hasMoved = false
                    moveToEdgeOrFadeOut()

                    return@TouchListener moved
                }

                MotionEvent.ACTION_MOVE -> {
                    val xDiff = (event.rawX - initTouchX).toInt()
                    val yDIff = (event.rawY - initTouchY).toInt()

                    if (xDiff.absoluteValue > movedThreshold || yDIff.absoluteValue > movedThreshold) {
                        hasMoved = true
                    }

                    val nextX = (initX + (if (isAlignParentLeft) xDiff else -xDiff))
                        .coerceAtLeast(0)
                    val nextY = (initY + (if (isAlignParentTop) yDIff else -yDIff))
                        .coerceAtLeast(0)

                    changeViewPosition(nextX, nextY)
                }
            }

            return@TouchListener false
        }

    private val isAlignParentLeft: Boolean
        get() = Gravity.getAbsoluteGravity(layoutGravity, rootView.layoutDirection) and
                Gravity.HORIZONTAL_GRAVITY_MASK == Gravity.LEFT

    private val isAlignParentTop: Boolean
        get() = layoutGravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.TOP

    private fun moveToEdgeOrFadeOut() {
        when {
            moveToEdgeAfterMoved -> moveToEdge()
            fadeOutAfterMoved -> fadeOut()
            else -> cancelFadeOut()
        }
    }

    //region Moving to edge
    private fun moveToEdge() {
        val params = params

        val edgePosition = getEdgePosition(params.x, params.y)

        moveTo(params.x, params.y, edgePosition[0], edgePosition[1], true)
    }

    private fun getEdgePosition(currentX: Int, currentY: Int): IntArray {
        val screenWidth = UIUtils.realDisplayMetrics.widthPixels

        val viewWidth = rootView.width
        val viewCenterX = currentX + viewWidth / 2

        val margin = moveToEdgeMargin

        val edgeX =
            // near left
            if (viewCenterX < screenWidth / 2) margin
            // near right
            else screenWidth - viewWidth - margin

        return intArrayOf(edgeX, currentY)
    }

    private var moveEdgeAnimator: ValueAnimator? = null

    private fun moveTo(
        currentX: Int,
        currentY: Int,
        destPositionX: Int,
        destPositionY: Int,
        withAnimation: Boolean
    ) {
        val currentParams = params
        if (!withAnimation) {
            if (currentParams.x != destPositionX || currentParams.y != destPositionY) {
                changeViewPosition(destPositionX, destPositionY)
            }
        } else {
            moveEdgeAnimator = ValueAnimator.ofInt(0, 100).apply {
                addUpdateListener { animation ->
                    val progress = animation.animatedValue as Int / 100f

                    val nextX = currentX + (destPositionX - currentX) * progress
                    val nextY = currentY + (destPositionY - currentY) * progress

                    changeViewPosition(nextX.toInt(), nextY.toInt())
                }

                duration = moveToEdgeDuration
                interpolator = OvershootInterpolator(1.25f)
                addListener(
                    onEnd = {
                        if (fadeOutAfterMoved) fadeOut()
                    },
                )

                start()
            }
        }
    }
    //endregion

    //region Fade-out
    private var fadeOutAnimator: ValueAnimator? = null

    private fun fadeOut() {
        logger.debug("fadeOut()")
        cancelFadeOut()

        fadeOutAnimator = ValueAnimator.ofFloat(fromAlpha, fadeOutDestinationAlpha).apply {
            addUpdateListener { animation ->
                rootView.alpha = animation.animatedValue as Float
            }
            duration = fadeOutAnimationDuration
            startDelay = fadeOutDelay

            start()
        }
    }

    private fun cancelFadeOut() {
        logger.debug("cancelFadeOut(): fadeOutAnimator: $fadeOutAnimator")
        fadeOutAnimator?.cancel()

        rootView.alpha = fromAlpha
    }

    protected fun rescheduleFadeOut() {
        logger.debug("rescheduleFadeOut()")
        cancelFadeOut()
        if (fadeOutAfterMoved) fadeOut()
    }
    //endregion
}
