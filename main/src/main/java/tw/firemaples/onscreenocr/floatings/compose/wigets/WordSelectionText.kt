package tw.firemaples.onscreenocr.floatings.compose.wigets

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.WordBoundary
import java.util.Locale

@Composable
fun WordSelectionText(
    modifier: Modifier = Modifier,
    text: String,
    locale: Locale,
    textStyle: TextStyle = LocalTextStyle.current,
    selectedSpanStyle: SpanStyle = SpanStyle(),
    onTextSelected: (String) -> Unit
) {
    var selectedStart by remember { mutableStateOf(-1) }
    val annotatedString = buildText(
        fullText = text,
        textAll = stringResource(id = R.string.text_all_text),
        locale = locale,
        selectedStart = selectedStart,
        selectedSpanStyle = selectedSpanStyle,
    )
    ClickableText(
        modifier = modifier,
        style = textStyle,
        text = annotatedString,
        onClick = { offset ->
            val clicked = annotatedString.getStringAnnotations(offset, offset)
                .firstOrNull()
            if (clicked != null) {
                selectedStart = clicked.start
                onTextSelected.invoke(clicked.tag)
            }
        },
    )
}

private fun buildText(
    fullText: String,
    textAll: String,
    locale: Locale,
    selectedStart: Int,
    selectedSpanStyle: SpanStyle
) = buildAnnotatedString {
    if (fullText.isEmpty()) return@buildAnnotatedString

    val text = "$textAll $fullText"

    val boundaries = WordBoundary.breakWords(text = text, locale = locale)
    if (boundaries.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }

    val unselectedStyle = SpanStyle(
        textDecoration = TextDecoration.Underline,
    )
    val selectedStyle = selectedSpanStyle.copy(
        textDecoration = TextDecoration.Underline
    )

    var textStart = 0
    var index = 0
    while (textStart < text.length || index < boundaries.size) {
        val nextBoundary = boundaries.getOrNull(index)
        if (nextBoundary == null) {
            append(text.substring(textStart until text.length))
            textStart = text.length
        } else if (textStart < nextBoundary.start) {
            append(text.substring(textStart until nextBoundary.start))
            textStart = nextBoundary.start
        } else if (textStart == nextBoundary.start) {
            val style = if (textStart == selectedStart)
                selectedStyle else unselectedStyle

            if (nextBoundary.start < textAll.length) {
                while (true) {
                    val next = boundaries.getOrNull(index + 1)
                    if (next == null || next.start >= textAll.length) {
                        break
                    }
                    index++
                }

                withStyle(style = style) {
                    pushStringAnnotation(tag = fullText, annotation = textAll)
                    append(textAll)
                }
                textStart = textAll.length
            } else {
                val word = text.substring(nextBoundary.start until nextBoundary.end)
                withStyle(style = style) {
                    pushStringAnnotation(tag = word, annotation = word)
                    append(word)
                }
                textStart = nextBoundary.end
            }
            index++
        }
    }
}

@Preview
@Composable
private fun WordBreakTextPreview() {
    val text = "  Hello  world test! word-breaker !  "
    val locale = Locale.US
    WordSelectionText(
        text = text,
        locale = locale,
        onTextSelected = {},
    )
}
