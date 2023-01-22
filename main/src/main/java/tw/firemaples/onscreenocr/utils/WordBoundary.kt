package tw.firemaples.onscreenocr.utils

import java.text.BreakIterator
import java.util.*

object WordBoundary {
    fun breakWords(text: String, locale: Locale): List<Boundary> {
        val iterator = BreakIterator.getWordInstance(locale).apply { setText(text) }
        val boundaries = mutableListOf<Boundary>()
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            val word = text.substring(start, end)
            if (word.isPunctuation() || word.isBlank()) {
                // Skip punctuations and blanks
            } else {
                if (boundaries.size >= 2 && boundaries.last().word.isDash()) {
                    val sb = StringBuilder(boundaries.removeLast().word)
                    val lastWord = boundaries.removeLast()
                    sb.insert(0, lastWord.word)
                    sb.append(word)
                    boundaries.add(Boundary(sb.toString(), lastWord.start, end))
                } else {
                    if (boundaries.isNotEmpty() && boundaries.last().word.isDash()) {
                        boundaries.removeLast()
                    }
                    boundaries.add(Boundary(word, start, end))
                }
            }

            start = end
            end = iterator.next()
        }

        return boundaries
    }

    private fun String.isPunctuation(): Boolean =
        this.length == 1 && CharCategory.OTHER_PUNCTUATION.contains(this[0])

    private fun String.isDash(): Boolean =
        this.length == 1 && CharCategory.DASH_PUNCTUATION.contains(this[0])

    data class Boundary(val word: String, val start: Int, val end: Int)
}
