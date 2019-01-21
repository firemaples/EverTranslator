package tw.firemaples.onscreenocr.translate

import kotlinx.coroutines.Job

interface Translator {
    fun translate(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit): Job
}