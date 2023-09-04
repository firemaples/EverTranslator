package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chibatching.kotpref.livedata.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.floatings.manager.FloatingStateManager
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.repo.GeneralRepository
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.SingleLiveEvent
import tw.firemaples.onscreenocr.utils.Utils
import java.util.Locale

typealias OCRText = Pair<String, String>

fun OCRText.text(): String = this.first
fun OCRText.locale(): Locale = Locale.forLanguageTag(this.second)
fun OCRText.langCode(): String = this.second

class ResultViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    private val _displayOCROperationProgress = MutableLiveData<Boolean>()
    val displayOCROperationProgress: LiveData<Boolean> = _displayOCROperationProgress

    private val _displayTranslationProgress = MutableLiveData<Boolean>()
    val displayTranslationProgress: LiveData<Boolean> = _displayTranslationProgress

    private val _ocrText = MutableLiveData<OCRText?>()
    val ocrText: LiveData<OCRText?> = _ocrText

    private val _translatedText = MutableLiveData<Pair<String, Int>?>()
    val translatedText: LiveData<Pair<String, Int>?> = _translatedText

    val displaySelectableText: LiveData<Boolean> =
        AppPref.asLiveData(AppPref::displaySelectedTextOnResultWindow)

    private val _displayRecognitionBlock = MutableLiveData<Boolean>()
    val displayRecognitionBlock: LiveData<Boolean> = _displayRecognitionBlock

    private val _displayTranslationBlock = MutableLiveData<Boolean>()
    val displayTranslatedBlock: LiveData<Boolean> = _displayTranslationBlock

    private val _translationProviderText = MutableLiveData<String?>()
    val translationProviderText: LiveData<String?> = _translationProviderText

    private val _displayTranslatedByGoogle = MutableLiveData<Boolean>()
    val displayTranslatedByGoogle: LiveData<Boolean> = _displayTranslatedByGoogle

    private val _displayRecognizedTextAreas = MutableLiveData<Pair<List<Rect>, Rect>>()
    val displayRecognizedTextAreas: LiveData<Pair<List<Rect>, Rect>> = _displayRecognizedTextAreas

    private val _copyRecognizedText = SingleLiveEvent<String>()
    val copyRecognizedText: LiveData<String> = _copyRecognizedText

    private val _displayTextInfoSearchView = SingleLiveEvent<TextInfoSearchViewData>()
    val displayTextInfoSearchView: LiveData<TextInfoSearchViewData> = _displayTextInfoSearchView

    val fontSize: LiveData<Float> = AppPref.asLiveData(AppPref::resultWindowFontSize)

    private val logger: Logger by lazy { Logger(ResultViewModel::class) }

    private val context: Context by lazy { Utils.context }

    private val repo: GeneralRepository by lazy { GeneralRepository() }

    private var lastLangCode: String = Constants.DEFAULT_OCR_LANG
    private var lastTextBoundingBoxes: List<Rect> = listOf()

//    companion object {
//        private const val STATE_RECOGNIZING = 0
//        private const val STATE_RECOGNIZED = 0
//        private const val STATE_TRANSLATING = 0
//        private const val STATE_TRANSLATED = 0
//    }

    fun startRecognition() {
        viewScope.launch {
            _displayRecognizedTextAreas.value = emptyList<Rect>() to Rect()

            _displayOCROperationProgress.value = true
            _displayTranslationProgress.value = false

            _ocrText.value = null
            _translatedText.value = null

            _displayRecognitionBlock.value = true
            _displayTranslationBlock.value = false
            _translationProviderText.value = null
            _displayTranslatedByGoogle.value = false
        }
    }

    fun textRecognized(result: RecognitionResult, parent: Rect, selected: Rect, viewRect: Rect) {
        viewScope.launch {
            this@ResultViewModel.lastLangCode = result.langCode

            _displayOCROperationProgress.value = false
            _ocrText.value = result.result to result.langCode

            val topOffset = parent.top + selected.top - viewRect.top
            val leftOffset = parent.left + selected.left - viewRect.left
            this@ResultViewModel.lastTextBoundingBoxes = result.boundingBoxes.toList()
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
            _displayRecognizedTextAreas.value = textAreas to unionRect

            if (repo.isAutoCopyOCRResult().first()) {
                _copyRecognizedText.value = result.result
            }
        }
    }

    fun startTranslation(translationProviderType: TranslationProviderType) {
        viewScope.launch {
            if (!translationProviderType.nonTranslation) {
                _displayTranslationBlock.value = true
                _displayTranslationProgress.value = true
                _displayTranslatedByGoogle.value = false
                _translationProviderText.value = null
            }

            when (translationProviderType) {
                TranslationProviderType.MicrosoftAzure ->
                    _translationProviderText.value =
                        "${context.getString(R.string.text_translated_by)} " +
                                context.getString(translationProviderType.nameRes)

                TranslationProviderType.GoogleMLKit ->
                    _displayTranslatedByGoogle.value = true

                TranslationProviderType.GoogleTranslateApp,
                TranslationProviderType.BingTranslateApp,
                TranslationProviderType.PapagoTranslateApp,
                TranslationProviderType.YandexTranslateApp,
                TranslationProviderType.OtherTranslateApp,
                TranslationProviderType.OCROnly -> {
                }
            }
        }
    }

    fun textTranslated(result: Result) {
        viewScope.launch {
            _displayTranslationProgress.value = false

            when (result) {
                is Result.Translated -> {
                    _translatedText.value = result.translatedText to R.color.foregroundSecond

                    if (repo.hideRecognizedTextAfterTranslated().first()) {
                        _displayRecognitionBlock.value = false
                    }
                }

                is Result.SourceLangNotSupport -> {
                    _translatedText.value =
                        context.getString(R.string.msg_translator_provider_does_not_support_the_ocr_lang) to R.color.alert
                }

                is Result.OCROnly -> {
                }
            }
        }
    }

    fun onOCRTextEdited(text: String) {
        viewScope.launch {
            val langCode = _ocrText.value!!.langCode()
            _ocrText.value = text to langCode

//            val langCode = try {
//                LanguageIdentify.identifyLanguage(text)
//            } catch (e: Exception) {
//                logger.debug(t = e)
//                null
//            } ?: lastLangCode

            FloatingStateManager.startTranslation(
                RecognitionResult(
                    langCode = langCode,
                    result = text,
                    boundingBoxes = lastTextBoundingBoxes,
                )
            )
        }
    }

    fun onTextSelectableChecked(checked: Boolean) {
        viewScope.launch {
            AppPref.displaySelectedTextOnResultWindow = checked
        }
    }

    fun onWordSelected(word: String) {
        viewScope.launch {
            _displayTextInfoSearchView.value = TextInfoSearchViewData(
                text = word,
                sourceLang = AppPref.selectedOCRLang,
                targetLang = AppPref.selectedTranslationLang,
            )
        }
    }

    data class TextInfoSearchViewData(
        val text: String,
        val sourceLang: String,
        val targetLang: String,
    )
}
