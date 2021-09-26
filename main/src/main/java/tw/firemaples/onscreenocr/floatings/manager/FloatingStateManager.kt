package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.floatings.main.MainBar
import tw.firemaples.onscreenocr.floatings.screenCircling.ScreenCirclingView
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

object FloatingStateManager {
    private val logger: Logger by lazy { Logger(FloatingStateManager::class) }
    private val context: Context by lazy { Utils.context }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentState = MutableStateFlow(State.Idle)
    val currentStateFlow: StateFlow<State> = _currentState
    val currentState: State
        get() = _currentState.value

    private val mainBar: MainBar by lazy { MainBar(context) }
    private val screenCirclingView: ScreenCirclingView by lazy {
        ScreenCirclingView(context).apply {
            onAreaSelected = { parent, selected ->
                this@FloatingStateManager.onAreaSelected(parent, selected)
            }
        }
    }

    val isMainBarAttached: Boolean
        get() = mainBar.attached

    private var parentRect: Rect? = null
    private var selectedRect: Rect? = null
    private var croppedBitmap: Bitmap? = null

    fun showMainBar() {
        mainBar.attachToScreen()
    }

    fun hideMainBar() {
        mainBar.detachFromScreen()
    }

    private fun arrangeMainBarToTop() {
        mainBar.detachFromScreen()
        mainBar.attachToScreen()
    }

    fun startScreenCircling() = stateIn(State.Idle) {
        logger.debug("startScreenCircling()")
        changeState(State.ScreenCircling)
        screenCirclingView.attachToScreen()
        arrangeMainBarToTop()
    }

    private fun onAreaSelected(parentRect: Rect, selectedRect: Rect) =
        stateIn(State.ScreenCircling) {
            logger.debug("onAreaSelected(), parentRect: $parentRect, selectedRect: $selectedRect")
            changeState(State.ScreenCircled)
            this@FloatingStateManager.selectedRect = selectedRect
            this@FloatingStateManager.parentRect = parentRect
        }

    fun cancelScreenCircling() = stateIn(State.ScreenCircling, State.ScreenCircled) {
        logger.debug("cancelScreenCircling()")
        changeState(State.Idle)
        screenCirclingView.detachFromScreen()
    }

    fun startScreenCapturing() = stateIn(State.ScreenCircled) {
        val parent = parentRect ?: return@stateIn
        val selected = selectedRect ?: return@stateIn
        logger.debug("startScreenCapturing(), parentRect: $parent, selectedRect: $selected")
        changeState(State.ScreenCapturing)
//        screenCirclingView.detachFromScreen()
        try {
            val croppedBitmap =
                ScreenExtractor.extractBitmapFromScreen(parentRect = parent, cropRect = selected)
            if (croppedBitmap != null) {
                this@FloatingStateManager.croppedBitmap = croppedBitmap
                startRecognition()
            } else {

            }
        } catch (t: Throwable) {
            logger.debug(t = t)

        }
        screenCirclingView.detachFromScreen()
    }

    private fun startRecognition() = stateIn(State.ScreenCapturing) {
        changeState(State.ErrorDisplaying)
        changeState(State.Idle)
    }

    private fun stateIn(vararg states: State, block: suspend CoroutineScope.() -> Unit) {
        scope.launch {
            if (states.contains(currentState)) block.invoke(this)
            else logger.error(t = IllegalStateException("The state should be in ${states.toList()}, current is $currentState"))
        }
    }

    private suspend fun changeState(newState: State) {
        val allowedNextStates: Array<State> = when (currentState) {
            State.Idle -> arrayOf(State.ScreenCircling)
            State.ScreenCircling -> arrayOf(State.Idle, State.ScreenCircled)
            State.ScreenCircled -> arrayOf(State.Idle, State.ScreenCapturing)
            State.ScreenCapturing -> arrayOf(State.TextRecognizing, State.ErrorDisplaying)
            State.TextRecognizing -> arrayOf(State.TextTranslating, State.ErrorDisplaying)
            State.TextTranslating -> arrayOf(State.ResultDisplaying, State.ErrorDisplaying)
            State.ResultDisplaying -> arrayOf(State.Idle)
            State.ErrorDisplaying -> arrayOf(State.Idle)
        }

        if (allowedNextStates.contains(newState)) {
            logger.debug("Change state $currentState > $newState")
            _currentState.value = newState
        } else {
            logger.debug("Change state from $currentState to $newState is not allowed")
        }
    }
}

enum class State {
    Idle,
    ScreenCircling,
    ScreenCircled,
    ScreenCapturing,
    TextRecognizing,
    TextTranslating,
    ResultDisplaying,
    ErrorDisplaying,
}
