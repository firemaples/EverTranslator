package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.di.MainCoroutineScope
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.compose.mainbar.MainBarFloatingView
import tw.firemaples.onscreenocr.floatings.dialog.showErrorDialog
import tw.firemaples.onscreenocr.floatings.result.ResultView
import tw.firemaples.onscreenocr.floatings.screenCircling.ScreenCirclingView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref
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
import kotlin.reflect.KClass

@Singleton
class FloatingViewCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val stateNavigator: StateNavigator,
    @MainCoroutineScope private val scope: CoroutineScope,
    private val mainBar: MainBarFloatingView,
    private val resultView: ResultView,
) {
    private val logger: Logger by lazy { Logger(FloatingViewCoordinator::class) }

    private val currentNavState: NavState
        get() = stateNavigator.currentNavState.value

    private val screenCirclingView: ScreenCirclingView by lazy {
        ScreenCirclingView(context).apply {
            onAreaSelected = { parent, selected ->
                this@FloatingViewCoordinator.onAreaSelected(parent, selected)
            }
        }
    }

    val showingStateChangedFlow = MutableStateFlow(false)
    val isMainBarAttached: Boolean
        get() = mainBar.attached

    private var selectedOCRLang: String = Constants.DEFAULT_OCR_LANG
    private val selectedOCRProvider: TextRecognitionProviderType get() = AppPref.selectedOCRProvider
    private var parentRect: Rect? = null
    private var selectedRect: Rect? = null
    private var croppedBitmap: Bitmap? = null

    init {
        stateNavigator.navigationAction
            .onEach {
                when (it) {
                    NavigationAction.NavigateToIdle ->
                        backToIdle()

                    NavigationAction.NavigateToScreenCircling ->
                        startScreenCircling()

                    is NavigationAction.NavigateToScreenCircled ->
                        onAreaSelected(
                            parentRect = it.parentRect,
                            selectedRect = it.selectedRect,
                        )

                    is NavigationAction.NavigateToScreenCapturing ->
                        startScreenCapturing()

                    is NavigationAction.NavigateToTextRecognition ->
                        startRecognition(
                            croppedBitmap = it.croppedBitmap,
                            parent = it.parent,
                            selected = it.selected,
                        )

                    is NavigationAction.NavigateToStartTranslation ->
                        startTranslation(it.recognitionResult)

                    is NavigationAction.NavigateToTranslated ->
                        showResult(it.result)

                    is NavigationAction.ShowError ->
                        showError(it.error)

                    NavigationAction.CancelScreenCircling ->
                        cancelScreenCircling()
                }
            }
            .launchIn(scope)

        resultView.onUserDismiss = {
            this@FloatingViewCoordinator.backToIdle()
        }
    }

    fun showMainBar() {
        if (isMainBarAttached) return
        mainBar.attachToScreen()
        scope.launch {
            showingStateChangedFlow.emit(true)
        }
    }

    private fun hideMainBar() {
        if (!isMainBarAttached) return
        mainBar.detachFromScreen()
        scope.launch {
            showingStateChangedFlow.emit(false)
        }
    }

    private fun arrangeMainBarToTop() {
        mainBar.detachFromScreen()
        mainBar.attachToScreen()
    }

    fun detachAllViews() {
        backToIdle()
        scope.launch {
            hideMainBar()
            FloatingView.detachAllFloatingViews()
        }
    }

    private fun startScreenCircling() = checkNextState(NavState.ScreenCircling::class) {
        if (!Translator.getTranslator().checkResources(scope)) {
            return@checkNextState
        }

        logger.debug("startScreenCircling()")
        stateNavigator.updateState(NavState.ScreenCircling)
        FirebaseEvent.logStartAreaSelection()
        screenCirclingView.attachToScreen()
        arrangeMainBarToTop()
    }

    private fun onAreaSelected(parentRect: Rect, selectedRect: Rect) =
        checkNextState(NavState.ScreenCircled::class) {
            logger.debug(
                "onAreaSelected(), parentRect: $parentRect, " +
                        "selectedRect: $selectedRect, " +
                        "size: ${selectedRect.width()}x${selectedRect.height()}"
            )
            if (currentNavState != NavState.ScreenCircled) {
                stateNavigator.updateState(NavState.ScreenCircled)
            }
            this@FloatingViewCoordinator.selectedRect = selectedRect
            this@FloatingViewCoordinator.parentRect = parentRect
        }

    private fun cancelScreenCircling() = checkNextState(NavState.Idle::class) {
        logger.debug("cancelScreenCircling()")
        stateNavigator.updateState(NavState.Idle)
        screenCirclingView.detachFromScreen()
    }

    private fun startScreenCapturing() =
        checkNextState(NavState.ScreenCapturing::class) {
            if (!Translator.getTranslator().checkResources(scope)) {
                return@checkNextState
            }

            this@FloatingViewCoordinator.selectedOCRLang = AppPref.selectedOCRLang
            val parent = parentRect ?: return@checkNextState
            val selected = selectedRect ?: return@checkNextState
            logger.debug("startScreenCapturing(), parentRect: $parent, selectedRect: $selected")
            stateNavigator.updateState(NavState.ScreenCapturing)
            mainBar.detachFromScreen()
            screenCirclingView.detachFromScreen()

            delay(100L)

            try {
                FirebaseEvent.logStartCaptureScreen()
                val croppedBitmap =
                    ScreenExtractor.extractBitmapFromScreen(
                        parentRect = parent,
                        cropRect = selected
                    )
                this@FloatingViewCoordinator.croppedBitmap = croppedBitmap
                FirebaseEvent.logCaptureScreenFinished()

                mainBar.attachToScreen()

                startRecognition(croppedBitmap, parent, selected)
            } catch (t: TimeoutCancellationException) {
                logger.debug(t = t)
                showError(context.getString(R.string.error_capture_screen_timeout))
                FirebaseEvent.logCaptureScreenFailed(t)
            } catch (t: Throwable) {
                logger.debug(t = t)
                showError(
                    t.message ?: context.getString(R.string.error_unknown_error_capturing_screen)
                )
                FirebaseEvent.logCaptureScreenFailed(t)
            }
//        screenCirclingView.detachFromScreen() // To test circled area
        }

    private fun startRecognition(croppedBitmap: Bitmap, parent: Rect, selected: Rect) =
        checkNextState(NavState.TextRecognizing::class) {
            stateNavigator.updateState(NavState.TextRecognizing)
            try {
                resultView.startRecognition()
                val recognizer = TextRecognizer.getRecognizer(selectedOCRProvider)
                FirebaseEvent.logStartOCR(recognizer.name)
                var result = withContext(Dispatchers.Default) {
                    recognizer.recognize(
                        TextRecognizer.getLanguage(selectedOCRLang, selectedOCRProvider)!!,
                        croppedBitmap
                    )
                }
                logger.debug("On text recognized: $result")
//                croppedBitmap.recycle() // to be used in the text editor view
                if (SettingManager.removeSpacesInCJK) {
                    val cjkLang = arrayOf("zh", "ja", "ko")
                    if (cjkLang.contains(selectedOCRLang.split("-").getOrNull(0))) {
                        result = result.copy(
                            result = result.result.replace(" ", "")
                        )
                    }
                    logger.debug("Remove CJK spaces: $result")
                }
                FirebaseEvent.logOCRFinished(recognizer.name)
                resultView.textRecognized(result, parent, selected, croppedBitmap)
                startTranslation(result)
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
                    TextRecognizer.getRecognizer(selectedOCRProvider).name, e
                )
            }
        }

    private fun startTranslation(recognitionResult: RecognitionResult) =
        checkNextState(NavState.TextTranslating::class) {
            try {
                stateNavigator.updateState(NavState.TextTranslating)

                val translator = Translator.getTranslator()

                resultView.startTranslation(translator.type)

                FirebaseEvent.logStartTranslationText(
                    recognitionResult.result,
                    recognitionResult.langCode,
                    translator
                )

                val translationResult = translator
                    .translate(recognitionResult.result, recognitionResult.langCode)

                when (translationResult) {
                    TranslationResult.OuterTranslatorLaunched -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        backToIdle()
                    }

                    is TranslationResult.SourceLangNotSupport -> {
                        FirebaseEvent.logTranslationSourceLangNotSupport(
                            translator, recognitionResult.langCode,
                        )
                        showResult(
                            Result.SourceLangNotSupport(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                                providerType = translationResult.type,
                            )
                        )
                    }

                    TranslationResult.OCROnlyResult -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        showResult(
                            Result.OCROnly(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                            )
                        )
                    }

                    is TranslationResult.TranslatedResult -> {
                        FirebaseEvent.logTranslationTextFinished(translator)
                        showResult(
                            Result.Translated(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                                translatedText = translationResult.result,
                                providerType = translationResult.type,
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
            } catch (e: Exception) {
                logger.warn(t = e)
                FirebaseEvent.logException(e)
                showError(e.message ?: "Unknown error found while translating")
            }
        }

    private fun showResult(result: Result) =
        checkNextState(NavState.ResultDisplaying::class) {
            logger.debug("showResult(), $result")
            stateNavigator.updateState(NavState.ResultDisplaying)

            resultView.textTranslated(result)
        }

    private fun showError(error: String) = checkNextState(NavState.ErrorDisplaying::class) {
        stateNavigator.updateState(NavState.ErrorDisplaying(error))
        logger.error(error)
        context.showErrorDialog(error)
        backToIdle()
    }

    private fun backToIdle() = checkNextState(NavState.Idle::class) {
        if (currentNavState != NavState.Idle) stateNavigator.updateState(NavState.Idle)
        croppedBitmap?.setReusable()
        resultView.backToIdle()
        showMainBar()
    }

    private fun checkNextState(
        vararg nextStates: KClass<out NavState>,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val notAllowed = nextStates.filterNot { stateNavigator.allowedNextState(it) }

        if (notAllowed.isEmpty()) {
            scope.launch { block.invoke(this) }
        } else {
            val error = "Transit from $notAllowed to $currentNavState is not allowed"
            logger.error(t = IllegalStateException(error))
        }
    }
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
