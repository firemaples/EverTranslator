package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.floatings.main.MainBar
import tw.firemaples.onscreenocr.floatings.screenCircling.ScreenCirclingView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.TranslationResult
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import kotlin.reflect.KClass

object FloatingStateManager {
    private val logger: Logger by lazy { Logger(FloatingStateManager::class) }
    private val context: Context by lazy { Utils.context }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentState = MutableStateFlow<State>(State.Idle)
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

    fun startScreenCircling() = stateIn(State.Idle::class) {
        if (!checkTranslationResources()) {
            return@stateIn
        }

        logger.debug("startScreenCircling()")
        changeState(State.ScreenCircling)
        screenCirclingView.attachToScreen()
        arrangeMainBarToTop()
    }

    private fun onAreaSelected(parentRect: Rect, selectedRect: Rect) =
        stateIn(State.ScreenCircling::class) {
            logger.debug("onAreaSelected(), parentRect: $parentRect, selectedRect: $selectedRect")
            changeState(State.ScreenCircled)
            this@FloatingStateManager.selectedRect = selectedRect
            this@FloatingStateManager.parentRect = parentRect
        }

    fun cancelScreenCircling() = stateIn(State.ScreenCircling::class, State.ScreenCircled::class) {
        logger.debug("cancelScreenCircling()")
        changeState(State.Idle)
        screenCirclingView.detachFromScreen()
    }

    fun startScreenCapturing() = stateIn(State.ScreenCircled::class) {
        val parent = parentRect ?: return@stateIn
        val selected = selectedRect ?: return@stateIn
        logger.debug("startScreenCapturing(), parentRect: $parent, selectedRect: $selected")
        changeState(State.ScreenCapturing)
        mainBar.detachFromScreen()
        screenCirclingView.detachFromScreen()
        try {
            val croppedBitmap =
                ScreenExtractor.extractBitmapFromScreen(parentRect = parent, cropRect = selected)
            this@FloatingStateManager.croppedBitmap = croppedBitmap

            mainBar.attachToScreen()

            startRecognition(croppedBitmap)
        } catch (t: TimeoutCancellationException) {
            logger.debug(t = t)
            showError("Capturing screen failed: timeout, please try it again later")
        } catch (t: Throwable) {
            logger.debug(t = t)
            showError(t.message ?: "Unknown error found while capturing screen")
        }
//        screenCirclingView.detachFromScreen() // To test circled area
    }

    private fun startRecognition(croppedBitmap: Bitmap) = stateIn(State.ScreenCapturing::class) {
        changeState(State.TextRecognizing)
        try {
            val result = TextRecognizer.getRecognizer().recognize(croppedBitmap)
            logger.debug("On text recognized: $result")
            startTranslation(result)
        } catch (e: Exception) {
            logger.warn(t = e)
            showError(e.message ?: "Unknown error found while recognizing text")
        }
    }

    private suspend fun checkTranslationResources(): Boolean {
        val translator = Translator.getTranslator()
        val langList = listOf(AppPref.selectedOCRLang, AppPref.selectedTranslationLang)

        val langToDownload = try {
            translator.checkResources(langList)
        } catch (e: Exception) {
            FirebaseEvent.logException(e)

            DialogView(context).apply {
                setTitle("Failed to check resources")
                setMessage("Failed reason: ${e.localizedMessage}")
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()
            return false
        }

        if (langToDownload.isNotEmpty()) {
            DialogView(context).apply {
                setTitle("Download")
                setMessage(
                    "The following language resources are not downloaded, do you want to download them now?" +
                            "\n\nModels: ${langToDownload.joinToString(", ")}"
                )
                setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

                onButtonOkClicked = {
                    downloadTranslationResources(translator, langToDownload)
                }
            }.attachToScreen()

            return false
        }
        return true
    }

    private fun downloadTranslationResources(translator: Translator, langList: List<String>) {
        scope.launch {
            val dialog = DialogView(context).apply {
                setTitle("Resources downloading")
                setMessage("The following resources is downloading, please wait.")
                setDialogType(DialogView.DialogType.CANCEL_ONLY)

                attachToScreen()
            }

            try {
                translator.downloadResources(langList)

                dialog.detachFromScreen()
                DialogView(context).apply {
                    setTitle("Resources downloaded")
                    setMessage("Resources downloaded, please click OK to continue.")
                    setDialogType(DialogView.DialogType.CONFIRM_ONLY)
                }.attachToScreen()
            } catch (e: Exception) {
                FirebaseEvent.logException(e)

                dialog.detachFromScreen()
                DialogView(context).apply {
                    setTitle("Downloading resources failed")
                    setMessage("Downloading resources failed, failed reason: ${e.localizedMessage}")
                    setDialogType(DialogView.DialogType.CONFIRM_ONLY)
                }.attachToScreen()
            }
        }
    }

    private fun startTranslation(recognitionResult: RecognitionResult) =
        stateIn(State.TextRecognizing::class) {
            try {
                changeState(State.TextTranslating)
                val translationResult = Translator.getTranslator()
                    .translate(recognitionResult.result, recognitionResult.langCode)

                when (translationResult) {
                    TranslationResult.OuterTranslatorLaunched -> backToIdle()
                    TranslationResult.OCROnlyResult ->
                        showResult(
                            Result.OCROnly(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                            )
                        )
                    is TranslationResult.TranslatedResult ->
                        showResult(
                            Result.Translated(
                                ocrText = recognitionResult.result,
                                boundingBoxes = recognitionResult.boundingBoxes,
                                translatedText = translationResult.result,
                                providerType = translationResult.type,
                            )
                        )
                    is TranslationResult.TranslationFailed -> {
                        FirebaseEvent.logException(translationResult.error)
                        DialogView(context).apply {
                            setTitle("Translation failed")
                            setMessage("Reason: ${translationResult.error.localizedMessage}")
                            setDialogType(DialogView.DialogType.CONFIRM_ONLY)

                            onButtonOkClicked = { backToIdle() }
                        }.attachToScreen()
                    }
                }
            } catch (e: Exception) {
                logger.warn(t = e)
                showError(e.message ?: "Unknown error found while translating")
            }
        }

    private fun showResult(result: Result) =
        stateIn(State.TextTranslating::class) {
            logger.debug("showResult(), $result")

            //TODO remove this line
            backToIdle()
        }

    private fun showError(error: String) {
        scope.launch {
            changeState(State.ErrorDisplaying(error))

            //TODO remove this line
            backToIdle()
        }
    }

    private fun backToIdle() =
        stateIn(
            State.TextTranslating::class,
            State.ResultDisplaying::class,
            State.ErrorDisplaying::class
        ) {
            changeState(State.Idle)
        }

    private fun stateIn(
        vararg states: KClass<out State>,
        block: suspend CoroutineScope.() -> Unit
    ) {
        scope.launch {
            if (states.contains(currentState::class)) block.invoke(this)
            else logger.error(t = IllegalStateException("The state should be in ${states.toList()}, current is $currentState"))
        }
    }

    private suspend fun changeState(newState: State) {
        val allowedNextStates = when (currentState) {
            State.Idle -> arrayOf(State.ScreenCircling::class)
            State.ScreenCircling -> arrayOf(State.Idle::class, State.ScreenCircled::class)
            State.ScreenCircled -> arrayOf(State.Idle::class, State.ScreenCapturing::class)
            State.ScreenCapturing ->
                arrayOf(State.TextRecognizing::class, State.ErrorDisplaying::class)
            State.TextRecognizing ->
                arrayOf(State.TextTranslating::class, State.ErrorDisplaying::class)
            State.TextTranslating ->
                arrayOf(
                    State.ResultDisplaying::class, State.ErrorDisplaying::class, State.Idle::class
                )
            State.ResultDisplaying -> arrayOf(State.Idle::class)
            is State.ErrorDisplaying -> arrayOf(State.Idle::class)
        }

        if (allowedNextStates.contains(newState::class)) {
            logger.debug("Change state $currentState > $newState")
            _currentState.value = newState
        } else {
            logger.error("Change state from $currentState to $newState is not allowed")
        }
    }
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

    data class OCROnly(
        override val ocrText: String,
        override val boundingBoxes: List<Rect>,
    ) : Result(ocrText, boundingBoxes)
}
