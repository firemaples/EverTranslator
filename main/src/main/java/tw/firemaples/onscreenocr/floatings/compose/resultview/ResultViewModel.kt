package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.Context
import android.graphics.Rect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.ResultInfo
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import javax.inject.Inject

interface ResultViewModel {
    val state: StateFlow<ResultViewState>
    val action: SharedFlow<ResultViewAction>
    fun onRootViewPositioned(xOffset: Int, yOffset: Int)
    fun onDialogOutsideClicked()
}

data class ResultViewState(
    val highlightArea: List<Rect> = listOf(),
    val highlightUnion: Rect = Rect(),
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
    val providerText: String? = null,
    val providerIcon: Int? = null,
)

sealed interface ResultViewAction {
    data object Close : ResultViewAction
}

class ResultViewModelImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
) : ResultViewModel {
    override val state = MutableStateFlow(ResultViewState())
    override val action = MutableSharedFlow<ResultViewAction>()

    private var rootViewXOffset: Int = 0
    private var rootViewYOffset: Int = 0

    init {
        stateNavigator.currentNavState
            .onEach { navState ->
                updateViewStateWithNavState(navState)
            }
            .launchIn(scope)
    }

    private fun updateViewStateWithNavState(navState: NavState) = scope.launch {
        when (navState) {
            is NavState.TextRecognizing ->
                state.update {
                    it.copy(
                        highlightArea = listOf(navState.selectedRect),
                        ocrState = it.ocrState.copy(
                            showProcessing = true,
                        )
                    )
                }

            is NavState.TextTranslating ->
                state.update {
                    val needTranslate = !navState.translationProviderType.nonTranslation
                    val (textAreas, unionArea) = calculateTextAreas(
                        navState.recognitionResult,
                        navState.parentRect,
                        navState.selectedRect,
                    )

                    it.copy(
                        highlightArea = textAreas,
                        highlightUnion = unionArea,
                        ocrState = it.ocrState.copy(
                            showProcessing = false,
                            ocrText = navState.recognitionResult.result,
                        ),
                        translationState = it.translationState.copy(
                            showTranslationArea = needTranslate,
                            showProcessing = needTranslate,
                        )
                    )
                }

            is NavState.TextTranslated -> {
                when (val resultInfo = navState.resultInfo) {
                    is ResultInfo.Error ->
                        setToDefault()

                    ResultInfo.OCROnly ->
                        state.update {
                            it.copy(
                                translationState = it.translationState.copy(
                                    showTranslationArea = false,
                                )
                            )
                        }

                    is ResultInfo.Translated -> {
                        val providerType = resultInfo.providerType
                        val needTranslate = !providerType.nonTranslation
                        val providerIcon =
                            if (providerType == TranslationProviderType.GoogleMLKit)
                                R.drawable.img_translated_by_google
                            else null

                        val providerLabel = if (providerIcon == null) {
                            val providerName = context.getString(providerType.nameRes)
                            "${context.getString(R.string.text_translated_by)} $providerName"
                        } else null

                        state.update {
                            it.copy(
                                translationState = it.translationState.copy(
                                    showTranslationArea = needTranslate,
                                    showProcessing = false,
                                    translatedText = resultInfo.translatedText,
                                    providerText = providerLabel,
                                    providerIcon = providerIcon,
                                )
                            )
                        }
                    }
                }
            }

            else -> {
                setToDefault()
            }
        }
    }

    private fun setToDefault() {
        state.value = ResultViewState()
    }

    private fun calculateTextAreas(
        result: RecognitionResult,
        parent: Rect,
        selected: Rect,
    ): Pair<List<Rect>, Rect> {
        val topOffset = parent.top + selected.top - rootViewYOffset
        val leftOffset = parent.left + selected.left - rootViewXOffset
        val textAreas = result.boundingBoxes.map {
            Rect(
                it.left + leftOffset,
                it.top + topOffset,
                it.right + leftOffset,
                it.bottom + topOffset
            )
        }
        val unionRect = Rect()
        textAreas.forEach { unionRect.union(it) }

        return textAreas to unionRect
    }

    override fun onRootViewPositioned(xOffset: Int, yOffset: Int) {
        rootViewXOffset = xOffset
        rootViewYOffset = yOffset
    }

    override fun onDialogOutsideClicked() {
        scope.launch {
            action.emit(ResultViewAction.Close)
            stateNavigator.navigate(NavigationAction.NavigateToIdle(showMainBar = true))
        }
    }
}
