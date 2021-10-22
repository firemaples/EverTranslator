package tw.firemaples.onscreenocr.floatings.screenCircling

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.CountDownTimer
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.Logger

class ProgressBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {
    companion object {
        private const val ANIM_TIME = 1000L
        private const val ANIM_INTERVAL = 10L
        private const val DEBUG = false
    }

    private val logger: Logger by lazy { Logger(ProgressBorderView::class) }

    private val paint: Paint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.circlingView_progressBorder)
        strokeWidth = context.resources.getDimensionPixelSize(
            R.dimen.circlingView_progressBorderWidth
        ).toFloat()
        style = Paint.Style.STROKE
    }

    @Volatile
    private var progress = 0
    private var timer: CountDownTimer? = null

    fun start() = synchronized(this) {
        progress = 0
        stop()

        timer = object : CountDownTimer(ANIM_TIME, ANIM_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                progress = ((ANIM_TIME - millisUntilFinished).toFloat() /
                        ANIM_TIME.toFloat() * 100f).toInt()

                if (DEBUG) logger.info("onTick(), progress: $progress")

                invalidate()
            }

            override fun onFinish() {
                progress = 100

                if (DEBUG) logger.info("onFinish(), progress: $progress")

                invalidate()
            }
        }.start()
    }

    fun stop() = synchronized(this) {
        timer?.cancel()
        timer = null
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isInEditMode) return

        drawBorder(canvas, progress)
    }

    private fun drawBorder(canvas: Canvas, progress: Int) {
        if (DEBUG) logger.info("drawBorder(), progress: $progress")

        // The percent of drawing the top border.
        val topSteps = 25
        // The percent of drawing the left/right border.
        val centerSteps = 50
        // The percent of drawing the bottom border.
        val bottomSteps = 25

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        val topXStart = (width / 2)
        val topRunLength: Float = if (progress >= topSteps) {
            (width / 2)
        } else {
            width / 2f / topSteps.toFloat() * progress.toFloat()
        }
        // Draw the top-center to top-right border.
        canvas.drawLine(topXStart, 0f, topXStart + topRunLength, 0f, paint)
        // Draw the top-center to top-left border.
        canvas.drawLine(topXStart, 0f, topXStart - topRunLength, 0f, paint)

        // Draw the left/right border.
        if (progress > topSteps) {
            val leftRightYStartY = 0f
            val leftRightRunLength: Float = if (progress >= topSteps + centerSteps) {
                height
            } else {
                height / centerSteps.toFloat() * (progress - topSteps).toFloat()
            }
            //Draw the left border.
            canvas.drawLine(0f, leftRightYStartY, 0f, leftRightYStartY + leftRightRunLength, paint)
            //Draw the right border.
            canvas.drawLine(
                width,
                leftRightYStartY,
                width,
                leftRightYStartY + leftRightRunLength,
                paint
            )
        }

        // Draw the bottom border.
        if (progress > topSteps + centerSteps) {
            val bottomLeftXStart = 0f

            @Suppress("UnnecessaryVariable")
            val bottomRightXStart = width

            val bottomRunLength: Float = if (progress == 100) {
                (width / 2)
            } else {
                width / 2f / bottomSteps.toFloat() * (progress - topSteps - centerSteps).toFloat()
            }
            //Draw the bottom-center to bottom-right border.
            canvas.drawLine(
                bottomRightXStart,
                height,
                bottomRightXStart - bottomRunLength,
                height,
                paint
            )
            //draw the bottom-center to bottom-left border.
            canvas.drawLine(
                bottomLeftXStart,
                height,
                bottomLeftXStart + bottomRunLength,
                height,
                paint
            )
        }
    }
}
