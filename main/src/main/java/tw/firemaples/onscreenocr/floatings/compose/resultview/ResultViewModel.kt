package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.graphics.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import javax.inject.Inject

interface ResultViewModel {
    val state: StateFlow<ResultViewState>
    val action: SharedFlow<ResultViewAction>

    fun onDialogOutsideClicked()
}

data class ResultViewState(
    val highlightArea: Rect? = null,
    val ocrState: OCRState = OCRState(),
    val translationState: TranslationState = TranslationState(),
)

data class OCRState(
    val showProcessing: Boolean = false,
    val ocrText: String? = null,
    val textSearchEnabled: Boolean = false,
)

data class TranslationState(
    val showTranslationArea: Boolean = false,
    val showProcessing: Boolean = false,
    val translatedText: String? = null,
    val translationProviderText: String? = null,
    val translationProviderIcon: Int? = null,
)

//sealed interface ResultViewState {
//    data object Default : ResultViewState
//    data object TextRecognizing : ResultViewState
//    interface TextRecognized : ResultViewState {
//        val parentRect: Rect
//        val selectedRect: Rect
//        val ocrText: String
//    }
//
//    data class TextTranslating(
//        override val parentRect: Rect,
//        override val selectedRect: Rect,
//        override val ocrText: String,
//    ) : TextRecognized
//
//    data class OCROnlyResult(
//        override val parentRect: Rect,
//        override val selectedRect: Rect,
//        override val ocrText: String,
//    ) : TextRecognized
//
//    data class TranslationResult(
//        override val parentRect: Rect,
//        override val selectedRect: Rect,
//        override val ocrText: String,
//        val translatedText: String,
//    ) : TextRecognized
//}

sealed interface ResultViewAction {
    data object Close : ResultViewAction
}

class ResultViewModelImpl @Inject constructor(
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
) : ResultViewModel {
    override val state = MutableStateFlow<ResultViewState>(ResultViewState())
    override val action = MutableSharedFlow<ResultViewAction>()

//    init {
//        stateNavigator.currentNavState
//            .onEach { navState ->
//                updateViewStateWithNavState(navState)
//            }
//            .launchIn(scope)
//    }

//    private fun updateViewStateWithNavState(navState: NavState) = scope.launch {
//        when (navState) {
//            is NavState.TextRecognizing ->
//                state.value = ResultViewState.TextRecognizing
//
//            is NavState.TextTranslating ->
//                state.value = ResultViewState.TextTranslating(
//                    parentRect = navState.parentRect,
//                    selectedRect = navState.selectedRect,
//                    ocrText = navState.recognitionResult.result,
//                )
//
//            is NavState.TextTranslated ->
//                state.value = when (navState.resultInfo) {
//                    ResultInfo.OCROnly -> ResultViewState.OCROnlyResult(
//                        parentRect = navState.parentRect,
//                        selectedRect = navState.selectedRect,
//                        ocrText = navState.recognitionResult.result,
//                    )
//
//                    is ResultInfo.Translated -> ResultViewState.TranslationResult(
//                        parentRect = navState.parentRect,
//                        selectedRect = navState.selectedRect,
//                        ocrText = navState.recognitionResult.result,
//                        translatedText = navState.resultInfo.translatedText,
//                    )
//                    is ResultInfo.Error ->
//                        ResultViewState.Default //TODO
//                }
//
//            else -> {
//                setToDefault()
//            }
//        }
//    }
//
//    private fun setToDefault() {
//        scope.launch {
//            state.value = ResultViewState.Default
//        }
//    }

    override fun onDialogOutsideClicked() {
        scope.launch {
            action.emit(ResultViewAction.Close)
            stateNavigator.navigate(NavigationAction.NavigateToIdle(showMainBar = true))
        }
    }
}
