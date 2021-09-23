package tw.firemaples.onscreenocr.floatings.screenCircling

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.utils.Logger
import kotlin.math.max
import kotlin.math.min
import kotlin.properties.Delegates

class CirclingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val logger: Logger by lazy { Logger(CirclingView::class) }

    private val lineWidth =
        resources.getDimensionPixelSize(R.dimen.circlingView_strokeWidth).toFloat()

    private val boxCirclingPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.circlingView_circlingBox)
        strokeWidth = lineWidth
    }

    private val boxPaint = Paint(boxCirclingPaint).apply {
        color = ContextCompat.getColor(context, R.color.circlingView_box)
        style = Paint.Style.STROKE
    }

    var helperTextView: HelperTextView? = null

    var onAreaSelected: ((Rect) -> Unit)? = null

    private var startPoint: Point? = null
    private var endPoint: Point? = null

    var selectedBox: Rect? by Delegates.observable(null) { _, _, _ ->
        invalidate()
    }
    private var resizeBase: Rect = Rect()

    private val onGesture = object : OnGesture {
        override fun onAreaCreationStart(startPoint: Point) {
            FirebaseEvent.logDragSelectionArea()

            selectedBox = null
            helperTextView?.hasBox = false

            this@CirclingView.startPoint = startPoint
        }

        override fun onAreaCreationDragging(endPoint: Point) {
            this@CirclingView.endPoint = endPoint
            invalidate()

            helperTextView?.isDrawing = true
        }

        override fun onAreaCreationFinish(startPoint: Point, endPoint: Point) {
            val newBox = createNewBox(startPoint, endPoint)
            selectedBox = newBox
            this@CirclingView.startPoint = null
            this@CirclingView.endPoint = null

            invalidate()

            helperTextView?.apply {
                hasBox = true
                isDrawing = false
                startAnim()
            }

            onAreaSelected?.invoke(newBox)
        }

        override fun onAreaResizeStart() {
            FirebaseEvent.logResizeSelectionArea()

            if (selectedBox != null) {
                resizeBase = Rect(selectedBox)
            }
        }

        override fun onAreaResizing(leftDiff: Int, rightDiff: Int, topDiff: Int, bottomDiff: Int) {
            val box = selectedBox ?: return

            with(box) {
                left = resizeBase.left + leftDiff
                right = max(left + 1, resizeBase.right + rightDiff)
                top = resizeBase.top + topDiff
                bottom = max(top + 1, resizeBase.bottom + bottomDiff)
            }

            invalidate()

            helperTextView?.isDrawing = true
        }

        override fun onAreaResizeFinish() {
            helperTextView?.apply {
                isDrawing = false
                startAnim()
            }

            val box = selectedBox ?: return
            onAreaSelected?.invoke(box)
        }
    }

    private lateinit var gestureAdapter: CircleGestureAdapter

    init {
        if (!isInEditMode) gestureAdapter = CircleGestureAdapter(this, onGesture)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        helperTextView?.stopAnim()
        helperTextView = null
    }

    fun clear() {
        startPoint = null
        endPoint = null
        selectedBox = null
        invalidate()
    }

    private fun createNewBox(startPoint: Point, endPoint: Point): Rect {
        val x1 = startPoint.x
        val x2 = endPoint.x
        val y1 = startPoint.y
        val y2 = endPoint.y

        val left = min(x1, x2)
        val right = max(x1, x2)
        val top = min(y1, y2)
        val bottom = max(y1, y2)

        return Rect(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) return

//        canvas.save()

        val startPoint = startPoint
        val endPoint = endPoint

        if (startPoint != null && endPoint != null) {
            canvas.drawRect(createNewBox(startPoint, endPoint), boxCirclingPaint)
        }

        val box = selectedBox
        if (box != null) {
            canvas.drawRect(box, boxPaint)
        }

//        canvas.restore()
    }
}
