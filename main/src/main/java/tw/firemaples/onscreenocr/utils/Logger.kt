package tw.firemaples.onscreenocr.utils

import android.util.Log
import tw.firemaples.onscreenocr.BuildConfig
import kotlin.reflect.KClass

fun composeDebug(msg: String? = null, t: Throwable? = null) {
    Logger.log(Logger.COMPOSE_LOGGER, Logger.DEBUG, msg, t)
}

class Logger(clazz: KClass<*>) {
    companion object {
        const val DEBUG = 0
        const val INFO = 1
        const val WARN = 2
        const val ERROR = 3

        const val COMPOSE_LOGGER = "ComposeLogger"

        fun log(tag: String, level: Int, msg: String?, t: Throwable?) {
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

    private val tag: String = clazz.java.simpleName

    fun debug(msg: String? = null, t: Throwable? = null) = log(tag, DEBUG, msg, t)
    fun info(msg: String? = null, t: Throwable? = null) = log(tag, INFO, msg, t)
    fun warn(msg: String? = null, t: Throwable? = null) = log(tag, WARN, msg, t)
    fun error(msg: String? = null, t: Throwable? = null) = log(tag, ERROR, msg, t)
}
