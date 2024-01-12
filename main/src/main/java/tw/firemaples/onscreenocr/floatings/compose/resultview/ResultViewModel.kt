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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.BitmapIncluded
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.ResultInfo
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import javax.inject.Inject

interface ResultViewModel {
    val state: StateFlow<ResultViewState>
    val action: SharedFlow<ResultViewAction>
    fun onRootViewPositioned(xOffset: Int, yOffset: Int)
    fun onDialogOutsideClicked()
    fun onTextSearchClicked()
    fun onOCRTextEditClicked()
    fun onOCRTextEdited(text: String)
    fun onCopyClicked(textType: TextType)
    fun onAdjustFontSizeClicked()
    fun onGoogleTranslateClicked(textType: TextType)
    fun onShareOCRTextClicked()
}

data class ResultViewState(
    val highlightArea: List<Rect> = listOf(),
    val highlightUnion: Rect = Rect(),
    val textSearchEnabled: Boolean = false,
    val ocrState: OCRState = OCRState(),
    val translationState: TranslationState = TranslationState(),
)

data class OCRState(
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
    data object Close : ResultViewAction
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
            }
            .launchIn(scope)
    }

    private fun updateViewStateWithNavState(navState: NavState) = scope.launch {
        if (navState is BitmapIncluded) {
            this@ResultViewModelImpl.parentRect = navState.parentRect
            this@ResultViewModelImpl.selectedRect = navState.selectedRect
            this@ResultViewModelImpl.croppedBitmap = navState.bitmap
        }

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
                    this@ResultViewModelImpl.lastRecognitionResult = navState.recognitionResult

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

            NavState.Idle -> {
                setToDefault()
                action.emit(ResultViewAction.Close)
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
            stateNavigator.navigate(NavigationAction.NavigateToIdle())
        }
    }

    override fun onTextSearchClicked() {
        scope.launch {
            state.update {
                it.copy(
                    textSearchEnabled = it.textSearchEnabled.not(),
                )
            }
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
            //TODO subscribe to the font size changes
            action.emit(ResultViewAction.ShowFontSizeAdjuster)
        }
    }

    override fun onGoogleTranslateClicked(textType: TextType) {
        scope.launch {
            action.emit(ResultViewAction.LaunchGoogleTranslator(textType.getTargetText()))
            stateNavigator.navigate(NavigationAction.NavigateToIdle())
        }
    }

    override fun onShareOCRTextClicked() {
        scope.launch {
            action.emit(ResultViewAction.ShareText(state.value.ocrState.ocrText))
            stateNavigator.navigate(NavigationAction.NavigateToIdle())
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
