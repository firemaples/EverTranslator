package tw.firemaples.onscreenocr.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.android.Main

private val threadUI = Dispatchers.Main

val stateManagerAction = newSingleThreadContext("stateManagerAction")

val threadTranslation = newSingleThreadContext("threadTranslation")

fun threadUI(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(threadUI, block = block)