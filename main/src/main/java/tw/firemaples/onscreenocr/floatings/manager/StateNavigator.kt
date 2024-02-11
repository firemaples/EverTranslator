package tw.firemaples.onscreenocr.floatings.manager

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.floatings.compose.base.awaitForSubscriber
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

interface StateNavigator {
    val navigationAction: SharedFlow<NavigationAction>
    val currentNavState: StateFlow<NavState>
    suspend fun navigate(action: NavigationAction)

    fun allowedNextState(nextNavState: KClass<out NavState>): Boolean

    fun updateState(newNavState: NavState)
}

@Singleton
class StateNavigatorImpl @Inject constructor() : StateNavigator {
    private val logger: Logger by lazy { Logger(this::class) }

    override val navigationAction = MutableSharedFlow<NavigationAction>()

    override val currentNavState = MutableStateFlow<NavState>(NavState.Idle)

    private val nextStates: Map<KClass<out NavState>, Set<KClass<out NavState>>> = mapOf(
        NavState.Idle::class to setOf(
            NavState.Idle::class, NavState.ScreenCircling::class,
        ),
        NavState.ScreenCircling::class to setOf(
            NavState.Idle::class, NavState.ScreenCircled::class,
        ),
        NavState.ScreenCircled::class to setOf(
            NavState.Idle::class, NavState.ScreenCapturing::class, NavState.ScreenCircled::class,
        ),
        NavState.ScreenCapturing::class to setOf(
            NavState.Idle::class, NavState.TextRecognizing::class,
        ),
        NavState.TextRecognizing::class to setOf(
            NavState.Idle::class, NavState.TextTranslating::class,
        ),
        NavState.TextTranslating::class to setOf(
            NavState.TextTranslated::class, NavState.Idle::class,
        ),
        NavState.TextTranslated::class to setOf(
            NavState.Idle::class, NavState.TextTranslating::class,
        ),
    )

    override suspend fun navigate(action: NavigationAction) {
        logger.debug("Receive NavigationAction: $action")
        navigationAction.awaitForSubscriber()
        navigationAction.emit(action)
    }

    override fun allowedNextState(nextNavState: KClass<out NavState>): Boolean =
        nextStates[currentNavState.value::class]?.contains(nextNavState) == true

    override fun updateState(newNavState: NavState) {
        val allowedNextStates = nextStates[currentNavState.value::class]

        val transitionName =
            "${currentNavState.value::class.simpleName} > ${newNavState::class.simpleName}"
        val transitionInfo = "${currentNavState.value} > ${newNavState::class}"

        if (allowedNextStates?.contains(newNavState::class) == true) {
            logger.debug("Change state $transitionName, info: $transitionInfo")
            currentNavState.value = newNavState
        } else {
            logger.error("Change state from $transitionName is not allowed, info: $transitionInfo")
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
        val ocrLang: String,
        val ocrProvider: TextRecognitionProviderType,
    ) : NavigationAction

    data class NavigateToTextRecognition(
        val ocrLang: String,
        val ocrProvider: TextRecognitionProviderType,
        val croppedBitmap: Bitmap,
        val parentRect: Rect,
        val selectedRect: Rect,
    ) : NavigationAction

    data class NavigateToStartTranslation(
        val croppedBitmap: Bitmap,
        val parentRect: Rect,
        val selectedRect: Rect,
        val recognitionResult: RecognitionResult,
    ) : NavigationAction

    data class ReStartTranslation(
        val croppedBitmap: Bitmap,
        val parentRect: Rect,
        val selectedRect: Rect,
        val recognitionResult: RecognitionResult,
    ) : NavigationAction

    data class NavigateToTranslated(
        val croppedBitmap: Bitmap,
        val parentRect: Rect,
        val selectedRect: Rect,
        val recognitionResult: RecognitionResult,
        val translator: Translator,
        val translationResult: TranslationResult,
    ) : NavigationAction

    data class ShowError(
        val error: String,
    ) : NavigationAction
}

sealed class NavState {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }

    object Idle : NavState()
    object ScreenCircling : NavState()
    data class ScreenCircled(val parentRect: Rect, val selectedRect: Rect) : NavState()
    object ScreenCapturing : NavState()
    data class TextRecognizing(
        override val parentRect: Rect,
        override val selectedRect: Rect,
        val croppedBitmap: Bitmap,
    ) : NavState(), BitmapIncluded {
        override val bitmap: Bitmap
            get() = croppedBitmap
    }

    data class TextTranslating(
        override val parentRect: Rect,
        override val selectedRect: Rect,
        val croppedBitmap: Bitmap,
        val recognitionResult: RecognitionResult,
        val translationProviderType: TranslationProviderType,
    ) : NavState(), BitmapIncluded {
        override val bitmap: Bitmap
            get() = croppedBitmap
    }

    data class TextTranslated(
        override val parentRect: Rect,
        override val selectedRect: Rect,
        val croppedBitmap: Bitmap,
        val recognitionResult: RecognitionResult,
        val resultInfo: ResultInfo,
    ) : NavState(), BitmapIncluded {
        override val bitmap: Bitmap
            get() = croppedBitmap
    }
//    data class ErrorDisplaying(val error: String) : NavState()
}

interface BitmapIncluded {
    val parentRect: Rect
    val selectedRect: Rect
    val bitmap: Bitmap
}
