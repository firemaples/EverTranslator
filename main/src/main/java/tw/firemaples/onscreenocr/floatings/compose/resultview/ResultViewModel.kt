package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.data.usecase.GetCurrentTranslationLangUseCase
import tw.firemaples.onscreenocr.data.usecase.GetHidingOCRAreaAfterTranslatedUseCase
import tw.firemaples.onscreenocr.data.usecase.GetLimitResultViewMaxWidthUseCase
import tw.firemaples.onscreenocr.data.usecase.GetResultViewFontSizeUseCase
import tw.firemaples.onscreenocr.data.usecase.GetShowTextSelectorOnResultViewUseCase
import tw.firemaples.onscreenocr.data.usecase.SetShowTextSelectorOnResultViewUseCase
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.BitmapIncluded
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.ResultInfo
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import javax.inject.Inject

interface ResultViewModel {
    val state: StateFlow<ResultViewState>
    val action: SharedFlow<ResultViewAction>
    fun onRootViewPositioned(xOffset: Int, yOffset: Int)
    fun onDialogOutsideClicked()
    fun onHomeButtonPressed()
    fun onTextSearchClicked()
    fun onTextSearchWordSelected(word: String)
    fun onOCRTextEditClicked()
    fun onOCRTextEdited(text: String)
    fun onCopyClicked(textType: TextType)
    fun onAdjustFontSizeClicked()
    fun onGoogleTranslateClicked(textType: TextType)
    fun onShareOCRTextClicked()
}

data class ResultViewState(
    val limitMaxWidth: Boolean = true,
    val textSearchEnabled: Boolean = false,
    val fontSize: Float = Constants.DEFAULT_RESULT_WINDOW_FONT_SIZE,
    val highlightArea: List<Rect> = listOf(),
    val highlightUnion: Rect = Rect(),
    val ocrState: OCRState = OCRState(),
    val translationState: TranslationState = TranslationState(),
)

data class OCRState(
    val showRecognitionArea: Boolean = true,
    val showProcessing: Boolean = false,
    val ocrText: String = "",
)

data class TranslationState(
    val showTranslationArea: Boolean = false,
    val showProcessing: Boolean = false,
    val translatedText: String = "",
    val providerText: String? = null,
    val providerIcon: Int? = null,
)

sealed interface ResultViewAction {
    data class ShowOCRTextEditor(val text: String, val croppedBitmap: Bitmap) : ResultViewAction
    data object ShowFontSizeAdjuster : ResultViewAction
    data class LaunchGoogleTranslator(val text: String) : ResultViewAction
    data class ShareText(val text: String) : ResultViewAction
    data class ShowTextInfoSearchView(
        val text: String,
        val sourceLang: String,
        val targetLang: String,
    ) : ResultViewAction
}

enum class TextType {
    OCRText, TranslationResult
}

class ResultViewModelImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
    getShowTextSelectorOnResultViewUseCase: GetShowTextSelectorOnResultViewUseCase,
    private val setShowTextSelectorOnResultViewUseCase: SetShowTextSelectorOnResultViewUseCase,
    getResultViewFontSizeUseCase: GetResultViewFontSizeUseCase,
    private val getLimitResultViewMaxWidthUseCase: GetLimitResultViewMaxWidthUseCase,
    private val getCurrentTranslationLangUseCase: GetCurrentTranslationLangUseCase,
    private val getHidingOCRAreaAfterTranslatedUseCase: GetHidingOCRAreaAfterTranslatedUseCase,
) : ResultViewModel {
    private val logger by lazy { Logger(this::class) }

    override val state = MutableStateFlow(ResultViewState())
    override val action = MutableSharedFlow<ResultViewAction>()

    private var rootViewXOffset: Int = 0
    private var rootViewYOffset: Int = 0
    private var parentRect: Rect? = null
    private var selectedRect: Rect? = null
    private var croppedBitmap: Bitmap? = null
    private var lastRecognitionResult: RecognitionResult? = null

    init {
        stateNavigator.currentNavState
            .onEach { navState ->
                updateViewStateWithNavState(navState)
            }.launchIn(scope)

        getShowTextSelectorOnResultViewUseCase.invoke()
            .onEach { show ->
                state.update {
                    it.copy(
                        textSearchEnabled = show,
                    )
                }
            }.launchIn(scope)

        getResultViewFontSizeUseCase.invoke()
            .onEach { fontSize ->
                state.update {
                    it.copy(
                        fontSize = fontSize,
                    )
                }
            }.launchIn(scope)
    }

    private fun updateViewStateWithNavState(navState: NavState) = scope.launch {
        if (navState is BitmapIncluded) {
            this@ResultViewModelImpl.parentRect = navState.parentRect
            this@ResultViewModelImpl.selectedRect = navState.selectedRect
            this@ResultViewModelImpl.croppedBitmap = navState.bitmap
        }

        state.update {
            it.copy(
                limitMaxWidth = getLimitResultViewMaxWidthUseCase.invoke(),
            )
        }

        when (navState) {
            is NavState.TextRecognizing ->
                state.update {
                    it.copy(
                        highlightArea = listOf(navState.selectedRect),
                        highlightUnion = navState.selectedRect,
                        ocrState = it.ocrState.copy(
                            showProcessing = true,
                        )
                    )
                }

            is NavState.TextTranslating -> {
                state.update {
                    this@ResultViewModelImpl.lastRecognitionResult = navState.recognitionResult

                    val needTranslate = !navState.translationProviderType.nonTranslation
                    val (textAreas, unionArea) = calculateTextAreas(
                        navState.recognitionResult.boundingBoxes,
                        navState.parentRect,
                        navState.selectedRect,
                    )

                    it.copy(
                        highlightArea = textAreas,
                        highlightUnion = unionArea,
                        ocrState = it.ocrState.copy(
                            showRecognitionArea = true,
                            showProcessing = false,
                            ocrText = navState.recognitionResult.result,
                        ),
                        translationState = it.translationState.copy(
                            showTranslationArea = needTranslate,
                            showProcessing = needTranslate,
                        )
                    )
                }

                if (SettingManager.autoCopyOCRResult) {
                    val recognizedText = navState.recognitionResult.result
                    scope.launch {
                        Utils.copyToClipboard(
                            label = LABEL_RECOGNIZED_TEXT,
                            text = recognizedText,
                        )
                    }
                }
            }


            is NavState.TextTranslated -> {
                when (val resultInfo = navState.resultInfo) {
                    is ResultInfo.Error ->
                        clearData()

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
                        val showRecognitionArea =
                            getHidingOCRAreaAfterTranslatedUseCase.invoke().not()

                        state.update {
                            it.copy(
                                ocrState = it.ocrState.copy(
                                    showRecognitionArea = showRecognitionArea,
                                ),
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

            NavState.Idle -> {
                clearData()
            }

            else -> {
                clearData()
            }
        }
    }

    private fun clearData() {
        state.update {
            it.copy(
                highlightArea = listOf(),
                highlightUnion = Rect(),
                ocrState = OCRState(),
                translationState = TranslationState(),
            )
        }
    }

    private fun calculateTextAreas(
        boundingBoxes: List<Rect>,
        parent: Rect,
        selected: Rect,
    ): Pair<List<Rect>, Rect> {
        val topOffset = parent.top + selected.top - rootViewYOffset
        val leftOffset = parent.left + selected.left - rootViewXOffset
        val textAreas = boundingBoxes.map {
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
            stateNavigator.navigate(NavigationAction.NavigateToIdle)
        }
    }

    override fun onHomeButtonPressed() {
        scope.launch {
            stateNavigator.navigate(NavigationAction.NavigateToIdle)
        }
    }

    override fun onTextSearchClicked() {
        scope.launch {
            val show = state.value.textSearchEnabled.not()
            setShowTextSelectorOnResultViewUseCase.invoke(show)
        }
    }

    override fun onTextSearchWordSelected(word: String) {
        scope.launch {
            val sourceLang = lastRecognitionResult?.langCode ?: return@launch
            val targetLang = getCurrentTranslationLangUseCase.invoke().first()
            action.emit(
                ResultViewAction.ShowTextInfoSearchView(
                    text = word,
                    sourceLang = sourceLang,
                    targetLang = targetLang,
                )
            )
        }
    }

    override fun onOCRTextEditClicked() {
        scope.launch {
            val croppedBitmap = croppedBitmap ?: return@launch
            val text = state.value.ocrState.ocrText
            action.emit(
                ResultViewAction.ShowOCRTextEditor(
                    text = text,
                    croppedBitmap = croppedBitmap,
                )
            )
        }
    }

    override fun onOCRTextEdited(text: String) {
        scope.launch {
            val parentRect = parentRect ?: return@launch
            val selectedRect = selectedRect ?: return@launch
            val croppedBitmap = croppedBitmap ?: return@launch
            val recognitionResult = lastRecognitionResult ?: return@launch
            stateNavigator.navigate(
                NavigationAction.ReStartTranslation(
                    croppedBitmap = croppedBitmap,
                    parentRect = parentRect,
                    selectedRect = selectedRect,
                    recognitionResult = recognitionResult.copy(result = text),
                )
            )
        }
    }

    override fun onCopyClicked(textType: TextType) {
        scope.launch {
            val label = when (textType) {
                TextType.OCRText -> LABEL_RECOGNIZED_TEXT
                TextType.TranslationResult -> LABEL_TRANSLATED_TEXT
            }

            Utils.copyToClipboard(
                label = label,
                text = textType.getTargetText()
            )
        }
    }

    override fun onAdjustFontSizeClicked() {
        scope.launch {
            action.emit(ResultViewAction.ShowFontSizeAdjuster)
        }
    }

    override fun onGoogleTranslateClicked(textType: TextType) {
        scope.launch {
            action.emit(ResultViewAction.LaunchGoogleTranslator(textType.getTargetText()))
            stateNavigator.navigate(NavigationAction.NavigateToIdle)
        }
    }

    override fun onShareOCRTextClicked() {
        scope.launch {
            action.emit(ResultViewAction.ShareText(state.value.ocrState.ocrText))
            stateNavigator.navigate(NavigationAction.NavigateToIdle)
        }
    }

    private fun TextType.getTargetText(): String = when (this) {
        TextType.OCRText -> state.value.ocrState.ocrText
        TextType.TranslationResult -> state.value.translationState.translatedText
    }

    companion object {
        private const val LABEL_RECOGNIZED_TEXT = "Recognized text"
        private const val LABEL_TRANSLATED_TEXT = "Translated text"
    }
}
