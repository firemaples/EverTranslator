package tw.firemaples.onscreenocr.translate

import kotlinx.coroutines.experimental.Job

interface Translator {
    fun translate(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit): Job
}