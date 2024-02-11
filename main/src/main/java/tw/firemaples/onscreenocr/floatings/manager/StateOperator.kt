package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.di.MainCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.StateOperator.Companion.SCREENSHOT_DELAY
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.translator.azure.MicrosoftAzureTranslator
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.setReusable
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

interface StateOperator {
    val action: SharedFlow<StateOperatorAction>

    companion object {
        const val SCREENSHOT_DELAY = 200L
    }
}

@Singleton
class StateOperatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateNavigator: StateNavigator,
    @MainCoroutineScope
    private val scope: CoroutineScope,
) : StateOperator {
    private val logger: Logger by lazy { Logger(this::class) }

    override val action = MutableSharedFlow<StateOperatorAction>()

    private val currentNavState: NavState
        get() = stateNavigator.currentNavState.value

    init {
        stateNavigator.navigationAction
            .onEach { action ->
                logger.debug("Receive navigationAction: $action")
                when (action) {
                    NavigationAction.NavigateToScreenCircling ->
                        startScreenCircling()

                    is NavigationAction.NavigateToScreenCircled ->
                        onAreaSelected(
                            parentRect = action.parentRect,
                            selectedRect = action.selectedRect,
                        )

                    NavigationAction.CancelScreenCircling ->
                        cancelScreenCircling()

                    is NavigationAction.NavigateToScreenCapturing ->
                        startScreenCapturing(
                            ocrLang = action.ocrLang,
                            ocrProvider = action.ocrProvider,
                        )

                    is NavigationAction.ReStartTranslation -> {
                        startTranslation(
                            croppedBitmap = action.croppedBitmap,
                            parentRect = action.parentRect,
                            selectedRect = action.selectedRect,
                            recognitionResult = action.recognitionResult,
                        )
                    }

                    is NavigationAction.NavigateToIdle ->
                        backToIdle()

                    is NavigationAction.NavigateToTextRecognition ->
                        startRecognition(
                            ocrLang = action.ocrLang,
                            ocrProvider = action.ocrProvider,
                            croppedBitmap = action.croppedBitmap,
                            parentRect = action.parentRect,
                            selectedRect = action.selectedRect,
                        )

                    is NavigationAction.NavigateToStartTranslation ->
                        startTranslation(
                            croppedBitmap = action.croppedBitmap,
                            parentRect = action.parentRect,
                            selectedRect = action.selectedRect,
                            recognitionResult = action.recognitionResult,
                        )

                    is NavigationAction.NavigateToTranslated ->
                        onTranslated(
                            croppedBitmap = action.croppedBitmap,
                            parentRect = action.parentRect,
                            selectedRect = action.selectedRect,
                            recognitionResult = action.recognitionResult,
                            translator = action.translator,
                            translationResult = action.translationResult,
                        )

                    is NavigationAction.ShowError ->
                        showError(action.error)
                }
            }.launchIn(scope)
    }

    private fun startScreenCircling() = scope.launch {
        if (!Translator.getTranslator().checkResources(scope)) {
            return@launch
        }

        logger.debug("startScreenCircling()")
        stateNavigator.updateState(NavState.ScreenCircling)
        FirebaseEvent.logStartAreaSelection()

        action.emit(StateOperatorAction.ShowScreenCirclingView)
        action.emit(StateOperatorAction.TopMainBar)
    }

    private fun onAreaSelected(parentRect: Rect, selectedRect: Rect) = scope.launch {
        logger.debug(
            "onAreaSelected(), parentRect: $parentRect, " +
                    "selectedRect: $selectedRect," +
                    "selectedSize: ${selectedRect.width()}x${selectedRect.height()}"
        )

        stateNavigator.updateState(
            NavState.ScreenCircled(
                parentRect = parentRect, selectedRect = selectedRect,
            )
        )
    }

    private fun cancelScreenCircling() = scope.launch {
        logger.debug("cancelScreenCircling()")
        stateNavigator.updateState(NavState.Idle)
        action.emit(StateOperatorAction.HideScreenCirclingView)
    }

    private fun startScreenCapturing(
        ocrLang: String,
        ocrProvider: TextRecognitionProviderType,
    ) = scope.launch {
        if (!Translator.getTranslator().checkResources(scope)) {
            return@launch
        }

        val state = currentNavState
        if (state !is NavState.ScreenCircled) {
            val error = "State should be ScreenCircled but $state"
            logger.error(t = IllegalStateException(error))
            showError(error)
            return@launch
        }
        val parentRect = state.parentRect
        val selectedRect = state.selectedRect
        logger.debug(
            "startScreenCapturing(), " +
                    "parentRect: $parentRect, selectedRect: $selectedRect"
        )

        stateNavigator.updateState(NavState.ScreenCapturing)

        action.emit(StateOperatorAction.HideScreenCirclingView)

        delay(SCREENSHOT_DELAY)

        var bitmap: Bitmap? = null
        try {
            FirebaseEvent.logStartCaptureScreen()
            val croppedBitmap = ScreenExtractor.extractBitmapFromScreen(
                parentRect = parentRect,
                cropRect = selectedRect,
            ).also {
                bitmap = it
            }
            FirebaseEvent.logCaptureScreenFinished()

            stateNavigator.navigate(
                NavigationAction.NavigateToTextRecognition(
                    ocrLang = ocrLang,
                    ocrProvider = ocrProvider,
                    croppedBitmap = croppedBitmap,
                    parentRect = parentRect,
                    selectedRect = selectedRect,
                )
            )
        } catch (t: TimeoutCancellationException) {
            logger.debug(t = t)
            FirebaseEvent.logCaptureScreenFailed(t)
            showError(context.getString(R.string.error_capture_screen_timeout))
            bitmap?.setReusable()
        } catch (t: Throwable) {
            logger.debug(t = t)
            FirebaseEvent.logCaptureScreenFailed(t)
            val errorMsg =
                t.message ?: context.getString(R.string.error_unknown_error_capturing_screen)
            showError(errorMsg)
            bitmap?.setReusable()
        }
    }

    private fun startRecognition(
        ocrLang: String,
        ocrProvider: TextRecognitionProviderType,
        croppedBitmap: Bitmap,
        parentRect: Rect,
        selectedRect: Rect,
    ) = scope.launch {
        stateNavigator.updateState(
            NavState.TextRecognizing(
                parentRect = parentRect,
                selectedRect = selectedRect,
                croppedBitmap = croppedBitmap,
            )
        )

        try {
            action.emit(StateOperatorAction.ShowResultView)

            val recognizer = TextRecognizer.getRecognizer(ocrProvider)
            val language = TextRecognizer.getLanguage(ocrLang, ocrProvider)!!

            FirebaseEvent.logStartOCR(recognizer.name)
            var result = withContext(Dispatchers.Default) {
                recognizer.recognize(
                    lang = language,
                    bitmap = croppedBitmap,
                )
            }
            logger.debug("On text recognized: $result")
//                croppedBitmap.recycle() // to be used in the text editor view

            // TODO move logic
            if (SettingManager.removeSpacesInCJK) {
                val cjkLang = arrayOf("zh", "ja", "ko")
                if (cjkLang.contains(ocrLang.split("-").getOrNull(0))) {
                    result = result.copy(
                        result = result.result.replace(" ", "")
                    )
                }
                logger.debug("Remove CJK spaces: $result")
            }

            FirebaseEvent.logOCRFinished(recognizer.name)

            stateNavigator.navigate(
                NavigationAction.NavigateToStartTranslation(
                    croppedBitmap = croppedBitmap,
                    parentRect = parentRect,
                    selectedRect = selectedRect,
                    recognitionResult = result,
                )
            )
        } catch (e: Exception) {
            val error =
                if (e.message?.contains(Constants.errorInputImageIsTooSmall) == true) {
                    context.getString(R.string.error_selected_area_too_small)
                } else
                    e.message
                        ?: context.getString(R.string.error_an_unknown_error_found_while_recognition_text)

            logger.warn(t = e)
            showError(error)
            FirebaseEvent.logOCRFailed(
                TextRecognizer.getRecognizer(ocrProvider).name, e
            )
        }
    }

    private fun startTranslation(
        croppedBitmap: Bitmap,
        parentRect: Rect,
        selectedRect: Rect,
        recognitionResult: RecognitionResult,
    ) = scope.launch {
        try {
            val translator = Translator.getTranslator()

            stateNavigator.updateState(
                NavState.TextTranslating(
                    parentRect = parentRect,
                    selectedRect = selectedRect,
                    croppedBitmap = croppedBitmap,
                    recognitionResult = recognitionResult,
                    translationProviderType = translator.type,
                )
            )

            FirebaseEvent.logStartTranslationText(
                text = recognitionResult.result,
                fromLang = recognitionResult.langCode,
                translator = translator,
            )

            val translationResult = translator.translate(
                text = recognitionResult.result,
                sourceLangCode = recognitionResult.langCode,
            )

            stateNavigator.navigate(
                NavigationAction.NavigateToTranslated(
                    croppedBitmap = croppedBitmap,
                    parentRect = parentRect,
                    selectedRect = selectedRect,
                    recognitionResult = recognitionResult,
                    translator = translator,
                    translationResult = translationResult,
                )
            )
        } catch (e: Exception) {
            logger.warn(t = e)
            FirebaseEvent.logException(e)
            showError(e.message ?: "Unknown error found while translating")
        }
    }

    private fun onTranslated(
        croppedBitmap: Bitmap,
        parentRect: Rect,
        selectedRect: Rect,
        recognitionResult: RecognitionResult,
        translator: Translator,
        translationResult: TranslationResult,
    ) {
        when (translationResult) {
            TranslationResult.OuterTranslatorLaunched -> {
                FirebaseEvent.logTranslationTextFinished(translator)
                backToIdle()
            }

            is TranslationResult.SourceLangNotSupport -> {
                FirebaseEvent.logTranslationSourceLangNotSupport(
                    translator, recognitionResult.langCode,
                )

                showError(context.getString(R.string.msg_translator_provider_does_not_support_the_ocr_lang))
            }

            TranslationResult.OCROnlyResult -> {
                FirebaseEvent.logTranslationTextFinished(translator)

                stateNavigator.updateState(
                    NavState.TextTranslated(
                        parentRect = parentRect,
                        selectedRect = selectedRect,
                        croppedBitmap = croppedBitmap,
                        recognitionResult = recognitionResult,
                        resultInfo = ResultInfo.OCROnly,
                    )
                )
            }

            is TranslationResult.TranslatedResult -> {
                FirebaseEvent.logTranslationTextFinished(translator)

                stateNavigator.updateState(
                    NavState.TextTranslated(
                        parentRect = parentRect,
                        selectedRect = selectedRect,
                        croppedBitmap = croppedBitmap,
                        recognitionResult = recognitionResult,
                        resultInfo = ResultInfo.Translated(
                            translatedText = translationResult.result,
                            providerType = translationResult.type,
                        ),
                    )
                )
            }

            is TranslationResult.TranslationFailed -> {
                FirebaseEvent.logTranslationTextFailed(translator)
                val error = translationResult.error

                if (error is MicrosoftAzureTranslator.Error) {
                    FirebaseEvent.logMicrosoftTranslationError(error)
                }

                if (error is IOException) {
                    showError(context.getString(R.string.error_can_not_connect_to_translation_server))
                } else {
                    FirebaseEvent.logException(error)
                    showError(
                        error.localizedMessage
                            ?: context.getString(R.string.error_unknown)
                    )
                }
            }
        }
    }

    private fun showError(error: String) = scope.launch {
        logger.error("showError(): $error")
        backToIdle()
        action.emit(StateOperatorAction.ShowErrorDialog(error))
    }

    private fun backToIdle() = scope.launch {
        if (currentNavState != NavState.Idle)
            stateNavigator.updateState(NavState.Idle)

        action.emit(StateOperatorAction.HideResultView)

        currentNavState.getBitmap()?.setReusable()
    }

    private fun NavState.getBitmap(): Bitmap? =
        (this as? BitmapIncluded)?.bitmap
}

sealed interface StateOperatorAction {
    data object TopMainBar : StateOperatorAction
    data object ShowScreenCirclingView : StateOperatorAction
    data object HideScreenCirclingView : StateOperatorAction
    data object ShowResultView : StateOperatorAction
    data object HideResultView : StateOperatorAction
    data class ShowErrorDialog(val error: String) : StateOperatorAction
}

sealed class Result(
    open val ocrText: String,
    open val boundingBoxes: List<Rect>,
) {
    data class Translated(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
        val translatedText: String,
        val providerType: TranslationProviderType,
    ) : Result(ocrText, boundingBoxes)

    data class SourceLangNotSupport(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
        val providerType: TranslationProviderType,
    ) : Result(ocrText, boundingBoxes)

    data class OCROnly(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
    ) : Result(ocrText, boundingBoxes)
}

sealed interface ResultInfo {
    data class Translated(
        val translatedText: String,
        val providerType: TranslationProviderType,
    ) : ResultInfo

    data class Error(
        val providerType: TranslationProviderType,
        val resultError: ResultError,
    ) : ResultInfo

    data object OCROnly : ResultInfo
}

enum class ResultError {
    SourceLangNotSupport,
}
