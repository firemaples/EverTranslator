package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.R
import kotlin.properties.Delegates

class TextBoundingBoxView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    private val paint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(context, R.color.resultView_recognizedBoundingBoxes)
        }
    }

    var boundingBoxes: List<Rect> by Delegates.observable(emptyList()) { _, _, _ ->
        invalidate()
    }

//    private var rectanglesToDraw: List<Rect> = emptyList()
//
//    fun updateBoundingBoxes(boundingBoxes: List<Rect>) {
//
//    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        boundingBoxes.forEach { rect ->
            canvas.drawRect(rect, paint)
        }
    }
}
