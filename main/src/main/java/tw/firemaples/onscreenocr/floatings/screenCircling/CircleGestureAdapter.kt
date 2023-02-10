package tw.firemaples.onscreenocr.floatings.screenCircling

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import tw.firemaples.onscreenocr.utils.Logger
import kotlin.math.absoluteValue

class CircleGestureAdapter(view: View, private val callback: OnGesture) {
    companion object {
        private const val THRESHOLD_DETERMINE_FINGERS = 20
        private const val THRESHOLD_MIN_MOVE = 20
    }

    private val logger: Logger by lazy { Logger(CircleGestureAdapter::class) }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { _, event ->
        val action = event.action
        return@OnTouchListener when {
            (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                    && event.pointerCount <= 2 -> {
                initVariables()
                true
            }
            action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_CANCEL -> {
                when {
                    isOneFinger -> {
                        val startPoint = startPointForOne
                        val endPoint = endPointForOne
                        if (startPoint != null && endPoint != null) {
                            logger.debug("1 finger action finished")
                            callback.onAreaCreationFinish(startPoint, endPoint)
                        }
                    }
                    isTwoFingers -> {
                        logger.debug("2 finger action finished")
                        callback.onAreaResizeFinish()
                    }
                }
                initVariables()
                true
            }
            isOneFinger -> handleOneFingerTouch(event)
            event.pointerCount == 1 -> handleOneFingerTouch(event)
            event.pointerCount == 2 -> handleTwoFingersTouch(event)
            else -> true
        }
    }

    init {
        if (!view.isInEditMode) view.setOnTouchListener(onTouchListener)
    }

    private var isOneFinger = false
    private var isTwoFingers = false

    private var startPointForOne: Point? = null
    private var startPointForTwo: Array<Point>? = null
    private var endPointForOne: Point? = null

    private fun initVariables() {
        isOneFinger = false
        isTwoFingers = false

        startPointForOne = null
        startPointForTwo = null
        endPointForOne = null
    }

    private fun handleOneFingerTouch(event: MotionEvent): Boolean {
        if (isTwoFingers) return true

        if (event.action != MotionEvent.ACTION_MOVE) return true

        val point = Point(event.x.toInt(), event.y.toInt())
        val startPoint = startPointForOne
        if (startPoint == null) {
            logger.debug("Initialize 1 finger start point")
            startPointForOne = point
            return true
        }

        if (isOneFinger || overThreshold(point, startPoint)) {
            if (!isOneFinger) {
                logger.debug("Start 1 finger action")
                callback.onAreaCreationStart(startPoint)
            }

            isOneFinger = true
            endPointForOne = point

            logger.debug("Moving with 1 finger")
            callback.onAreaCreationDragging(point)
        }

        return true
    }

    private var leftIndex: Int = -1
    private var rightIndex: Int = -1
    private var topIndex: Int = -1
    private var bottomIndex: Int = -1
    private val fingerMoved: MutableMap<Int, Boolean> = mutableMapOf(0 to false, 1 to false)
    private fun handleTwoFingersTouch(event: MotionEvent): Boolean {
        if (isOneFinger) return true

        if (event.action != MotionEvent.ACTION_MOVE) return true
        if (event.pointerCount < 2) return true

        val point1 = Point(event.getX(0).toInt(), event.getY(0).toInt())
        val point2 = Point(event.getX(1).toInt(), event.getY(1).toInt())
        val startPoint = startPointForTwo
        if (startPoint == null) {
            logger.debug("Initialize 2 fingers start point")
            startPointForTwo = arrayOf(point1, point2)
            if (point1.x >= point2.x) {
                rightIndex = 0
                leftIndex = 1
            } else {
                rightIndex = 1
                leftIndex = 0
            }
            if (point1.y >= point2.y) {
                bottomIndex = 0
                topIndex = 1
            } else {
                bottomIndex = 1
                topIndex = 0
            }
            fingerMoved.keys.forEach { fingerMoved[it] = false }

            return true
        }

        if (isTwoFingers ||
            overThreshold(point1, startPoint[0]) || overThreshold(point2, startPoint[1])
        ) {
            if (!isTwoFingers) {
                logger.debug("Start 2 fingers action")
                callback.onAreaResizeStart()
            }

            isTwoFingers = true

            logger.debug("Moving 2 fingers")
            val endPoint = arrayOf(point1, point2)

            fingerMoved.filterValues { !it }.forEach {
                val i = it.key
                if (overThreshold(endPoint[i], startPoint[i])) {
                    fingerMoved[i] = true
                }
            }

            val leftDiff = if (fingerMoved[leftIndex] == true)
                endPoint[leftIndex].x - startPoint[leftIndex].x else 0

            val rightDiff = if (fingerMoved[rightIndex] == true)
                endPoint[rightIndex].x - startPoint[rightIndex].x else 0

            val topDiff = if (fingerMoved[topIndex] == true)
                endPoint[topIndex].y - startPoint[topIndex].y else 0

            val bottomDiff = if (fingerMoved[bottomIndex] == true)
                endPoint[bottomIndex].y - startPoint[bottomIndex].y else 0

            callback.onAreaResizing(leftDiff, rightDiff, topDiff, bottomDiff)
        }

        return true
    }

    private fun overThreshold(
        point1: Point, point2: Point, threshold: Int = THRESHOLD_MIN_MOVE
    ): Boolean =
        (point1.x - point2.x).absoluteValue > threshold ||
                (point1.y - point2.y).absoluteValue > threshold
}

interface OnGesture {
    fun onAreaCreationStart(startPoint: Point)
    fun onAreaCreationDragging(endPoint: Point)
    fun onAreaCreationFinish(startPoint: Point, endPoint: Point)
    fun onAreaResizeStart()
    fun onAreaResizing(leftDiff: Int, rightDiff: Int, topDiff: Int, bottomDiff: Int)
    fun onAreaResizeFinish()
}
