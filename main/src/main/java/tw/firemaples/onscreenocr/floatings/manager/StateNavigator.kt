package tw.firemaples.onscreenocr.floatings.manager

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton

interface StateNavigator {
    val navigationAction: SharedFlow<NavigationAction>
    val currentState: StateFlow<State>
    suspend fun navigate(action: NavigationAction)

    fun updateState(newState: State)
}

@Singleton
class StateNavigatorImpl @Inject constructor() : StateNavigator {
    private val logger: Logger by lazy { Logger(this::class) }

    override val navigationAction = MutableSharedFlow<NavigationAction>()

    override val currentState = MutableStateFlow<State>(State.Idle)

    override suspend fun navigate(action: NavigationAction) {
        navigationAction.subscriptionCount.first { it > 0 }
        navigationAction.emit(action)
    }

    override fun updateState(newState: State) {
        val allowedNextStates = when (currentState.value) {
            State.Idle -> arrayOf(State.ScreenCircling::class)
            State.ScreenCircling -> arrayOf(State.Idle::class, State.ScreenCircled::class)
            State.ScreenCircled -> arrayOf(State.Idle::class, State.ScreenCapturing::class)
            State.ScreenCapturing ->
                arrayOf(
                    State.Idle::class, State.TextRecognizing::class, State.ErrorDisplaying::class
                )

            State.TextRecognizing ->
                arrayOf(
                    State.Idle::class, State.TextTranslating::class, State.ErrorDisplaying::class
                )

            State.TextTranslating ->
                arrayOf(
                    State.ResultDisplaying::class, State.ErrorDisplaying::class, State.Idle::class
                )

            State.ResultDisplaying -> arrayOf(State.Idle::class, State.TextTranslating::class)
            is State.ErrorDisplaying -> arrayOf(State.Idle::class)
        }

        if (allowedNextStates.contains(newState::class)) {
            logger.debug("Change state ${currentState.value} > $newState")
            currentState.value = newState
        } else {
            logger.error("Change state from ${currentState.value} to $newState is not allowed")
        }
    }
}

sealed interface NavigationAction {
    data object NavigateToIdle : NavigationAction

    data object NavigateToScreenCircling : NavigationAction

    data class NavigateToScreenCircled(
        val parentRect: Rect,
        val selectedRect: Rect,
    ) : NavigationAction

    data object CancelScreenCircling : NavigationAction

    data class NavigateToScreenCapturing(
        val selectedOCRLang: String,
    ) : NavigationAction

    data class NavigateToTextRecognition(
        val croppedBitmap: Bitmap,
        val parent: Rect,
        val selected: Rect,
    ) : NavigationAction

    data class NavigateToStartTranslation(
        val recognitionResult: RecognitionResult,
    ) : NavigationAction

    data class NavigateToTranslated(
        val result: Result,
    ) : NavigationAction

    data class ShowError(
        val error: String,
    ) : NavigationAction
}

sealed class State {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }

    object Idle : State()
    object ScreenCircling : State()
    object ScreenCircled : State()
    object ScreenCapturing : State()
    object TextRecognizing : State()
    object TextTranslating : State()
    object ResultDisplaying : State()
    data class ErrorDisplaying(val error: String) : State()
}
