package tw.firemaples.onscreenocr.views

import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.math.absoluteValue

private const val THRESHOLD_DETERMINE_FINGERS = 20

class SelectionGestureAdapter(view: View, val callback: OnGesture) {
    private val logger: Logger = LoggerFactory.getLogger(SelectionGestureAdapter::class.java)

    private val onTouchListener = View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN && event.pointerCount <= 2) {
            initTouch()
            return@OnTouchListener true
        } else if (event.action == MotionEvent.ACTION_UP) {
            if (isOneFinger) {
                endPlaceForOne?.also { endPlace ->
                    firstPlaceForOne?.also { firstPlace ->
                        logger.debug("onAreaCreationFinish()")
                        callback.onAreaCreationFinish(firstPlace, endPlace)
                    }
                }
            } else if (isTwoFingers) {
                logger.debug("onAreaResizeFinish()")
                callback.onAreaResizeFinish()
            }
            initTouch()
            return@OnTouchListener true
        }

        if (isOneFinger) {
            handleOneFingerTouch(event)
            return@OnTouchListener true
        }

        when (event.pointerCount) {
            1 -> handleOneFingerTouch(event)
            2 -> handleTwoFingersTouch(event)
            else -> {
                true
            }
        }
    }

    init {
        if (!view.isInEditMode) {
            view.setOnTouchListener(onTouchListener)
        }
    }

    private var isOneFinger = false
    private var isTwoFingers = false

    private var firstPlaceForOne: Point? = null
    private var firstPlaceForTwo: Array<Point>? = null

    private var endPlaceForOne: Point? = null

    private fun initTouch() {
        isOneFinger = false
        isTwoFingers = false

        firstPlaceForOne = null
        firstPlaceForTwo = null

        endPlaceForOne = null
    }

    private fun determineFingers(point1: Point, point2: Point): Boolean =
            (point1.x - point2.x).absoluteValue > THRESHOLD_DETERMINE_FINGERS ||
                    (point1.y - point2.y).absoluteValue > THRESHOLD_DETERMINE_FINGERS

    private fun handleOneFingerTouch(event: MotionEvent): Boolean {
        if (isTwoFingers) return true

        val point = Point(event.x.toInt(), event.y.toInt())
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (firstPlaceForOne == null) {
                    logger.debug("1 finger first place")
                    firstPlaceForOne = point
                    return true
                }

                firstPlaceForOne?.also { firstPlace ->
                    if (isOneFinger || determineFingers(point, firstPlace)) {
                        if (!isOneFinger) {
                            logger.debug("onAreaCreationStart()")
                            callback.onAreaCreationStart(firstPlace)
                        }

                        isOneFinger = true

                        endPlaceForOne = point
                        logger.debug("onAreaCreationDragging()")
                        callback.onAreaCreationDragging(point)
                    }
                }
            }
        }

        return true
    }

    var leftIndex: Int = -1
    var rightIndex: Int = -1
    var topIndex: Int = -1
    var bottomIndex: Int = -1
    private fun handleTwoFingersTouch(event: MotionEvent): Boolean {
        if (isOneFinger) return true

        val point1 = Point(event.getX(0).toInt(), event.getY(0).toInt())
        val point2 = Point(event.getX(1).toInt(), event.getY(1).toInt())
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount < 2) return true
                if (firstPlaceForTwo == null) {
                    logger.debug("2 fingers first place")
                    firstPlaceForTwo = arrayOf(point1, point2)
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
                    return true
                }

                firstPlaceForTwo?.also {
                    if (isTwoFingers ||
                            determineFingers(point1, it[0]) || determineFingers(point2, it[1])) {
                        if (!isTwoFingers) {
                            logger.debug("onAreaResizeStart()")
                            callback.onAreaResizeStart()
                        }

                        isTwoFingers = true

                        firstPlaceForTwo?.also { firstPlace ->
                            logger.debug("onAreaResizing()")
                            val endPlace = arrayOf(point1, point2)
                            val leftDiff = endPlace[leftIndex].x - firstPlace[leftIndex].x
                            val rightDiff = endPlace[rightIndex].x - firstPlace[rightIndex].x
                            val topDiff = endPlace[topIndex].y - firstPlace[topIndex].y
                            val bottomDiff = endPlace[bottomIndex].y - firstPlace[bottomIndex].y

                            callback.onAreaResizing(leftDiff, rightDiff, topDiff, bottomDiff)
                        }
                    }
                }
            }
        }

        return true
    }
}

interface OnGesture {
    fun onAreaCreationStart(startPoint: Point)
    fun onAreaCreationDragging(endPoint: Point)
    fun onAreaCreationFinish(startPoint: Point, endPoint: Point)
    fun onAreaResizeStart()
    fun onAreaResizing(leftDiff: Int, rightDiff: Int, topDiff: Int, bottomDiff: Int)
    fun onAreaResizeFinish()
}