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

private const val DEBUG = false
private const val ANIM_TIME = 1000L
private const val ANIM_INTERVAL = 10L

@Suppress("ConstantConditionIf")
class ProgressBorderView : AppCompatImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    private val logger: Logger by lazy { LoggerFactory.getLogger(ProgressBorderView::class.java) }

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.captureAreaSelectionViewPaint_borderPaint)
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }

    @Volatile
    private var progress = 0
    private var timer: CountDownTimer? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode) {
            drawBorder(canvas, progress)
        }
    }

    fun start() = synchronized(this) {
        progress = 0
        stop()

        timer = object : CountDownTimer(ANIM_TIME, ANIM_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                progress = ((ANIM_TIME - millisUntilFinished).toFloat() /
                        ANIM_TIME.toFloat() * 100f).toInt()
                if (DEBUG) logger.info("progress: $progress")
                invalidate()
            }

            override fun onFinish() {
                progress = 100
                if (DEBUG) logger.info("progress: finished")
                invalidate()
            }
        }.start()
    }

    fun stop() = synchronized(this) {
        timer?.cancel()
        timer = null
    }

    private fun drawBorder(canvas: Canvas, progress: Int) {
        if (DEBUG) logger.info("drawBorder: progress: $progress")
        val topSteps = 25
        val centerSteps = 50
        val bottomSteps = 25

        val width = canvas.width
        val height = canvas.height

        val topRunLength: Float
        val topXStart = (width / 2).toFloat()
        if (progress >= topSteps) {
            topRunLength = (width / 2).toFloat()
        } else {
            topRunLength = width.toFloat() / 2f / topSteps.toFloat() * progress.toFloat()
        }
        //draw top-right
        canvas.drawLine(topXStart, 0f, topXStart + topRunLength, 0f, paint)
        //draw top-left
        canvas.drawLine(topXStart, 0f, topXStart - topRunLength, 0f, paint)

        if (progress > topSteps) {
            val leftRightYStartY = 0f
            val leftRightRunLength: Float = if (progress >= topSteps + centerSteps) {

                height.toFloat()
            } else {
                height.toFloat() / centerSteps.toFloat() * (progress - topSteps).toFloat()
            }
            //draw left
            canvas.drawLine(0f, leftRightYStartY, 0f, leftRightYStartY + leftRightRunLength, paint)
            //draw right
            canvas.drawLine(width.toFloat(), leftRightYStartY, width.toFloat(), leftRightYStartY + leftRightRunLength, paint)
        }

        if (progress > topSteps + centerSteps) {
            val bottomLeftXStart = 0f
            val bottomRightXStart = width.toFloat()

            val bottomRunLength: Float = if (progress == 100) {
                (width / 2).toFloat()
            } else {
                width.toFloat() / 2f / bottomSteps.toFloat() * (progress - topSteps - centerSteps).toFloat()
            }
            //draw bottom-right
            canvas.drawLine(bottomRightXStart, height.toFloat(), bottomRightXStart - bottomRunLength, height.toFloat(), paint)
            //draw bottom-left
            canvas.drawLine(bottomLeftXStart, height.toFloat(), bottomLeftXStart + bottomRunLength, height.toFloat(), paint)
        }
    }
}