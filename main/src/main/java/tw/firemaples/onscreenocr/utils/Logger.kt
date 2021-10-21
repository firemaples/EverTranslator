package tw.firemaples.onscreenocr.utils

import android.util.Log
import tw.firemaples.onscreenocr.BuildConfig
import kotlin.reflect.KClass

class Logger(clazz: KClass<*>) {
    companion object {
        private const val DEBUG = 0
        private const val INFO = 1
        private const val WARN = 2
        private const val ERROR = 3
    }

    private val tag: String = clazz.java.simpleName

    fun debug(msg: String? = null, t: Throwable? = null) = log(DEBUG, msg, t)
    fun info(msg: String? = null, t: Throwable? = null) = log(INFO, msg, t)
    fun warn(msg: String? = null, t: Throwable? = null) = log(WARN, msg, t)
    fun error(msg: String? = null, t: Throwable? = null) = log(ERROR, msg, t)

    private fun log(level: Int, msg: String?, t: Throwable?) {
        if (BuildConfig.DISABLE_LOGGING) return

        val threadName = Thread.currentThread().name
        val message = if (msg != null) "[$threadName] $msg" else "[$threadName]"

        when (level) {
            DEBUG -> Log.d(tag, message, t)
            INFO -> Log.i(tag, message, t)
            WARN -> Log.w(tag, message, t)
            ERROR -> Log.e(tag, message, t)
        }
    }
}
