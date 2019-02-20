package tw.firemaples.onscreenocr.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent

/**
 * Created by Firemaples on 2016/3/1.
 */

class AreaSelectionView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    @Suppress("unused")
    private val logger: Logger by lazy { LoggerFactory.getLogger(AreaSelectionView::class.java) }

    private var drawingStartPoint: Point? = null
    private var drawingEndPoint: Point? = null

    var box: Rect? = null
    var resizeBase: Rect = Rect()

    private var drawingLinePaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context,
                R.color.captureAreaSelectionViewPaint_drawingLinePaint)
        strokeWidth = 10f
    }
    private var boxPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.captureAreaSelectionViewPaint_boxPaint)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    var helpTextView: FadeOutHelpTextView? = null

    var callback: OnAreaSelectionViewCallback? = null

    private val onGesture = object : OnGesture {
        override fun onAreaCreationStart(startPoint: Point) {
            FirebaseEvent.logDragSelectionArea()

            box = null
            helpTextView?.hasBox = false

            drawingStartPoint = startPoint
        }

        override fun onAreaCreationDragging(endPoint: Point) {
            drawingEndPoint = endPoint
            invalidate()
            helpTextView?.isDrawing = true
        }

        override fun onAreaCreationFinish(startPoint: Point, endPoint: Point) {
            box = createNewBox(startPoint, endPoint)
            helpTextView?.hasBox = true
            drawingStartPoint = null
            drawingEndPoint = null

            invalidate()

            helpTextView?.isDrawing = false
            helpTextView?.startAnim()
            callback?.onAreaSelected(this@AreaSelectionView)
        }

        override fun onAreaResizeStart() {
            FirebaseEvent.logResizeSelectionArea()

            box?.also {
                resizeBase = Rect(it)
            }
        }

        override fun onAreaResizing(leftDiff: Int, rightDiff: Int, topDiff: Int, bottomDiff: Int) {
            box?.apply {
                left = resizeBase.left + leftDiff
                right = Math.max(left + 1, resizeBase.right + rightDiff)
                top = resizeBase.top + topDiff
                bottom = Math.max(top + 1, resizeBase.bottom + bottomDiff)

                invalidate()
                helpTextView?.isDrawing = true
            }
        }

        override fun onAreaResizeFinish() {
            helpTextView?.isDrawing = false
            helpTextView?.startAnim()
            callback?.onAreaSelected(this@AreaSelectionView)
        }
    }

    init {
        if (!isInEditMode) {
            SelectionGestureAdapter(this, onGesture)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        helpTextView?.stopAnim()
        helpTextView = null
    }

    fun getBoxList(): List<Rect> = box?.let { listOf(Rect(it)) } ?: listOf()

    fun clear() {
        drawingEndPoint = null
        drawingStartPoint = drawingEndPoint
        box = null
        invalidate()
    }

    fun setBoxList(boxList: List<Rect>) {
        if (boxList.isEmpty()) {
            box = null
            helpTextView?.hasBox = false
        } else {
            box = boxList[0]
            helpTextView?.hasBox = true
            helpTextView?.startAnim()
            callback?.onAreaSelected(this)
        }

        invalidate()
    }

    private fun createNewBox(startPoint: Point, endPoint: Point): Rect {
        val x1 = startPoint.x
        val x2 = endPoint.x
        val y1 = startPoint.y
        val y2 = endPoint.y

        val left = Math.min(x1, x2)
        val right = Math.max(x1, x2)
        val top = Math.min(y1, y2)
        val bottom = Math.max(y1, y2)

        return Rect(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode) {
            canvas.save()

            val drawingStartPoint = this.drawingStartPoint
            val drawingEndPoint = this.drawingEndPoint
            if (drawingStartPoint != null && drawingEndPoint != null) {
                canvas.drawRect(createNewBox(drawingStartPoint, drawingEndPoint), drawingLinePaint)
            }

            box?.also {
                canvas.drawRect(it, boxPaint)
            }

            canvas.restore()
        }
    }
}

interface OnAreaSelectionViewCallback {
    fun onAreaSelected(areaSelectionView: AreaSelectionView)
}
