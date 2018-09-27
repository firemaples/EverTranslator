package tw.firemaples.onscreenocr.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.CountDownTimer
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R

private const val DELAY_TEXT_HIDDEN_ANIM: Long = 2000
private const val TIME_TEXT_HIDDEN_ANIM: Long = 800
private const val INTERVAL_TEXT_HIDDEN_ANIM: Long = 10

class FadeOutHelpTextView : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    @Suppress("unused")
    private val logger: Logger by lazy { LoggerFactory.getLogger(FadeOutHelpTextView::class.java) }

    private var redrawHelpTextTimer: CountDownTimer? = null

    private val textPaint: Paint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimensionPixelSize(R.dimen.areaSelectionView_helpTextSize)
                .toFloat()
        color = ContextCompat.getColor(context,
                R.color.captureAreaSelectionViewPaint_helpTextPaint)
    }

    private var _hasBox = false
    var hasBox: Boolean
        get() = _hasBox
        set(value) {
            _hasBox = value
            invalidate()
        }

    private var _isDrawing = false
    var isDrawing: Boolean
        get() = _isDrawing
        set(value) {
            _isDrawing = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isInEditMode) {
            drawHelpText(canvas)
        }
    }

    private fun drawHelpText(canvas: Canvas) {
        if (isDrawing) return

        val message: String = if (hasBox) {
            context.getString(R.string.redrawAnAreaForTranslation)
        } else {
            textPaint.alpha = 255
            context.getString(R.string.drawAnAreaForTranslation)
        }

        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (textPaint.descent() + textPaint.ascent()) / 2).toInt()
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(message, xPos.toFloat(), yPos.toFloat(), textPaint)
    }

    fun clear() {

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnim()
    }

    fun startAnim() {
        stopAnim()
        redrawHelpTextTimer = object : CountDownTimer(TIME_TEXT_HIDDEN_ANIM + DELAY_TEXT_HIDDEN_ANIM, INTERVAL_TEXT_HIDDEN_ANIM) {
            private val MAX_ALPHA = 200

            override fun onTick(millisUntilFinished: Long) {
                val alpha: Int = if (millisUntilFinished < TIME_TEXT_HIDDEN_ANIM) {
                    (millisUntilFinished.toFloat() / TIME_TEXT_HIDDEN_ANIM.toFloat() * MAX_ALPHA.toFloat()).toInt()
                    //                    logger.info("Alpha: " + alpha);
                } else {
                    MAX_ALPHA
                    //                    logger.info("Alpha: fixed " + alpha);
                }
                textPaint.alpha = alpha
                //                logger.info("borderAnimationProgress: " + borderAnimationProgress);
                invalidate()
            }

            override fun onFinish() {
                textPaint.alpha = 0
                //                logger.info("borderAnimationProgress: finished");
                invalidate()
            }
        }.start()
    }

    fun stopAnim() {
        redrawHelpTextTimer?.cancel()
        redrawHelpTextTimer = null
    }
}