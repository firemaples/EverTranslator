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
import kotlin.reflect.KClass

interface StateNavigator {
    val navigationAction: SharedFlow<NavigationAction>
    val currentState: StateFlow<State>
    suspend fun navigate(action: NavigationAction)

    fun allowedNextState(nextState: KClass<out State>): Boolean

    fun updateState(newState: State)
}

@Singleton
class StateNavigatorImpl @Inject constructor() : StateNavigator {
    private val logger: Logger by lazy { Logger(this::class) }

    override val navigationAction = MutableSharedFlow<NavigationAction>()

    override val currentState = MutableStateFlow<State>(State.Idle)

    private val nextStates: Map<KClass<out State>, Set<KClass<out State>>> = mapOf(
        State.Idle::class to setOf(State.ScreenCircling::class),
        State.ScreenCircling::class to setOf(State.Idle::class, State.ScreenCircled::class),
        State.ScreenCircled::class to setOf(State.Idle::class, State.ScreenCapturing::class),
        State.ScreenCapturing::class to setOf(
            State.Idle::class, State.TextRecognizing::class, State.ErrorDisplaying::class,
        ),
        State.TextRecognizing::class to setOf(
            State.Idle::class, State.TextTranslating::class, State.ErrorDisplaying::class,
        ),
        State.TextTranslating::class to setOf(
            State.ResultDisplaying::class, State.ErrorDisplaying::class, State.Idle::class,
        ),
        State.ResultDisplaying::class to setOf(State.Idle::class, State.TextTranslating::class),
        State.ErrorDisplaying::class to setOf(State.Idle::class),
    )

    override suspend fun navigate(action: NavigationAction) {
        logger.debug("Receive NavigationAction: $action")
        navigationAction.subscriptionCount.first { it > 0 }
        navigationAction.emit(action)
    }

    override fun allowedNextState(nextState: KClass<out State>): Boolean =
        nextStates[currentState.value::class]?.contains(nextState) == true

    override fun updateState(newState: State) {
        val allowedNextStates = nextStates[currentState.value::class]

        if (allowedNextStates?.contains(newState::class) == true) {
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
