package tw.firemaples.onscreenocr.wigets

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.WordBoundary
import java.util.*

class WordBreakTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {
    private val logger: Logger by lazy { Logger(this::class) }

    private val wordSpans: MutableList<WordSpan> = mutableListOf()
    private var selectedSpan: WordSpan? = null
    var onWordClicked: ((String?) -> Unit)? = null

    fun setContent(text: String?, locale: Locale) {
        if (text == null) {
            clear()
            return
        }
        val spannable = breakWords(text, locale)
        this.movementMethod = LinkMovementMethod.getInstance()
        this.setText(spannable, BufferType.SPANNABLE)
    }

    fun clearSelection() {
        selectedSpan = null
        onWordClicked?.invoke(null)
        invalidate()
    }

    private fun clear() {
        text = null
        wordSpans.clear()
        selectedSpan = null
    }

    private fun breakWords(text: String, locale: Locale): SpannableString {
        wordSpans.clear()
        val boundaries = WordBoundary.breakWords(text, locale)
        logger.debug("Text Boundaries: $boundaries")

        val spannable = SpannableString(text)
        boundaries.forEach { boundary ->
            val span = WordSpan(this, boundary.word,
                selected = false,
                onWordClicked = {
                    if (it.selected) {
                        it.selected = false
                        selectedSpan = null
                    } else {
                        selectedSpan?.selected = false
                        selectedSpan = it
                        it.selected = true
                    }

                    onWordClicked?.invoke(selectedSpan?.text)
                })
            spannable.setSpan(span, boundary.start, boundary.end, 0)
            wordSpans.add(span)
        }

        return spannable
    }

    private class WordSpan(
        val textView: TextView,
        val text: String,
        var selected: Boolean,
        val onWordClicked: (WordSpan) -> Unit,
    ) :
        ClickableSpan() {

        override fun onClick(widget: View) {
            onWordClicked(this)
            widget.invalidate()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            if (selected) {
                ds.color = Color.BLUE
                ds.bgColor = Color.YELLOW
            } else {
                ds.color = textView.currentTextColor
            }
            textView.invalidate()
        }
    }
}
