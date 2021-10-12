package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Utils

class ResultViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    private val _displayOCROperationProgress = MutableLiveData<Boolean>()
    val displayOCROperationProgress: LiveData<Boolean> = _displayOCROperationProgress

    private val _displayTranslationProgress = MutableLiveData<Boolean>()
    val displayTranslationProgress: LiveData<Boolean> = _displayTranslationProgress

    private val _ocrText = MutableLiveData<String?>()
    val ocrText: LiveData<String?> = _ocrText

    private val _translatedText = MutableLiveData<String?>()
    val translatedText: LiveData<String?> = _translatedText

    private val _displayTranslationBlock = MutableLiveData<Boolean>()
    val displayTranslatedBlock: LiveData<Boolean> = _displayTranslationBlock

    private val _translationProviderText = MutableLiveData<String?>()
    val translationProviderText: LiveData<String?> = _translationProviderText

    private val _displayTranslatedByGoogle = MutableLiveData<Boolean>()
    val displayTranslatedByGoogle: LiveData<Boolean> = _displayTranslatedByGoogle

    private val context: Context by lazy { Utils.context }

//    companion object {
//        private const val STATE_RECOGNIZING = 0
//        private const val STATE_RECOGNIZED = 0
//        private const val STATE_TRANSLATING = 0
//        private const val STATE_TRANSLATED = 0
//    }

    fun startRecognition() {
        viewScope.launch {
            _displayOCROperationProgress.value = true
            _displayTranslationProgress.value = false

            _ocrText.value = null
            _translatedText.value = null

            _displayTranslationBlock.value = false
            _translationProviderText.value = null
            _displayTranslatedByGoogle.value = false
        }
    }

    fun textRecognized(result: RecognitionResult) {
        viewScope.launch {
            _displayOCROperationProgress.value = false
            _ocrText.value = result.result
        }
    }

    fun startTranslation(translationProviderType: TranslationProviderType) {
        viewScope.launch {
            if (!translationProviderType.nonTranslation) {
                _displayTranslationBlock.value = true
                _displayTranslationProgress.value = true
            }

            when (translationProviderType) {
                TranslationProviderType.MicrosoftAzure ->
                    _translatedText.value =
                        "translated by ${context.getString(translationProviderType.nameRes)}"
                TranslationProviderType.GoogleMLKit ->
                    _displayTranslatedByGoogle.value = true
                TranslationProviderType.GoogleTranslateApp,
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
                    _translatedText.value = result.translatedText
                }
                is Result.OCROnly -> {
                }
            }
        }
    }
}
