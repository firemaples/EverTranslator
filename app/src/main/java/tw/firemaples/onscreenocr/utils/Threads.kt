package tw.firemaples.onscreenocr.utils

import kotlinx.coroutines.*
import java.util.concurrent.Executors

private val threadUI = Dispatchers.Main

val stateManagerAction = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

val threadTranslation = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

fun threadUI(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(threadUI, block = block)

fun CoroutineDispatcher.launch(block: suspend CoroutineScope.() -> Unit): Job =
        GlobalScope.launch(this, block = block)