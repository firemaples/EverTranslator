package tw.firemaples.onscreenocr.translate

import android.content.Context
import kotlinx.coroutines.Job

interface Translator {
    fun translate(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit): Job

    fun checkResource(context: Context, source: String, target: String, callback: (result: Boolean) -> Unit) = callback(true)
}