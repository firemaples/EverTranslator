package tw.firemaples.onscreenocr.utils

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext

private val threadUI = HandlerContext(Handler(Looper.getMainLooper()))

val stateManagerAction = newSingleThreadContext("stateManagerAction")

val threadTranslation = newSingleThreadContext("threadTranslation")

fun threadUI(block: suspend CoroutineScope.() -> Unit): Job =
        launch(threadUI, block = block)