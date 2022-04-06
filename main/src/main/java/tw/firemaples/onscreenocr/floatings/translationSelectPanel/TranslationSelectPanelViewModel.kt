package tw.firemaples.onscreenocr.floatings.translationSelectPanel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.floatings.dialog.showDialog
import tw.firemaples.onscreenocr.floatings.dialog.showErrorDialog
import tw.firemaples.onscreenocr.recognition.RecognitionLanguage
import tw.firemaples.onscreenocr.recognition.Recognizer
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.repo.OCRRepository
import tw.firemaples.onscreenocr.repo.TranslationRepository
import tw.firemaples.onscreenocr.translator.TranslationProvider
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.firstPart

class TranslationSelectPanelViewModel(viewScope: CoroutineScope) :
    FloatingViewModel(viewScope) {

    private val _selectedOCRProviderName = MutableLiveData<String>()
    val selectedOCRProviderName: LiveData<String> = _selectedOCRProviderName

    private val _displayOCRProviders = MutableLiveData<List<TextRecognitionProviderType>>()

    private val _ocrLanguageList = MutableLiveData<List<LangItem>>()
    val ocrLanguageList: LiveData<List<LangItem>> = _ocrLanguageList

    private val _selectedTranslationProviderName = MutableLiveData<String>()
    val selectedTranslationProviderName: LiveData<String> = _selectedTranslationProviderName

    private val _displayTranslationProviders = MutableLiveData<List<TranslationProvider>>()
    val displayTranslateProviders: LiveData<List<TranslationProvider>> =
        _displayTranslationProviders

    private val _translationLangList = MutableLiveData<List<LangItem>>()
    val translationLangList: LiveData<List<LangItem>> = _translationLangList

    private val _displayTranslationHint = MutableLiveData<String?>()
    val displayTranslationHint: LiveData<String?> = _displayTranslationHint

    private val logger: Logger by lazy { Logger(TranslationSelectPanelViewModel::class) }
    private val context: Context by lazy { Utils.context }

    private val ocrRepo = OCRRepository()
    private val translationRepo = TranslationRepository()

    private var ocrLangList: List<RecognitionLanguage> = listOf()

    fun load() {
        viewScope.launch {
            val ocrLanguages = ocrRepo.getAllOCRLanguages().first()
            ocrLangList = ocrLanguages
            _ocrLanguageList.value = ocrLanguages
                .map { LangItem(it.code, it.displayName, it.selected, !it.downloaded) }

            val selectedTranslationProvider =
                translationRepo.getSelectedProvider().first()
            _selectedTranslationProviderName.value = selectedTranslationProvider.displayName

            loadTranslationLanguageList(selectedTranslationProvider.key)
        }
    }

    private suspend fun loadTranslationLanguageList(providerKey: String) {
        val translationLanguages =
            translationRepo.getTranslationLanguageList(providerKey).first()

        if (translationLanguages.isEmpty()) {
            _displayTranslationHint.value = translationRepo.getTranslationHint(providerKey).first()
            _translationLangList.value = emptyList()
        } else {
            val selectedOCRLang = ocrRepo.selectedOCRLangFlow.first()

            if (translationLanguages.any {
                    it.code.firstPart() == selectedOCRLang.firstPart()
                }) {
                _displayTranslationHint.value = null
                _translationLangList.value = translationLanguages
                    .map { LangItem(it.code, it.displayName, it.selected) }
            } else {
                _displayTranslationHint.value =
                    context.getString(R.string.msg_translator_provider_does_not_support_the_ocr_lang)
                _translationLangList.value = emptyList()
            }
        }
    }

    fun onOCRLangSelected(langItem: LangItem) {
        viewScope.launch {
            if (langItem.showDownloadIcon) {
                if (!downloadOCRModel(langItem)) {
                    return@launch
                }

                logger.debug("Download lang item success: $langItem")
            }

            ocrRepo.setSelectedOCRLanguage(langItem.code)
            val ocrLangList = _ocrLanguageList.value ?: return@launch
            _ocrLanguageList.value = ocrLangList.map {
                when {
                    it.code == langItem.code ->
                        it.copy(selected = true, showDownloadIcon = false)
                    it.selected ->
                        it.copy(selected = false)
                    else -> it
                }
            }

            loadTranslationLanguageList(translationRepo.selectedProviderTypeFlow.first().key)
        }
    }

    private suspend fun downloadOCRModel(langItem: LangItem): Boolean {
        val ocrLang = ocrLangList.firstOrNull { it.code == langItem.code } ?: return false
        val result = context.showDialog(
            title = "Download OCR model",
            message = "Do you want to download ${langItem.displayName} language model for OCR?",
            dialogType = DialogView.DialogType.CONFIRM_CANCEL,
            cancelByClickingOutside = true,
        )

        if (result) {
            when (val recognizer = ocrLang.recognizer) {
                Recognizer.Tesseract -> {
                    var cancelled = false
                    val dialogJob = viewScope.launch {
                        val downloadDialogResult = context.showDialog(
                            title = "OCR model downloading",
                            message = "Downloading OCR model: ${ocrLang.innerCode}[${langItem.displayName}]",
                            dialogType = DialogView.DialogType.CANCEL_ONLY,
                            cancelByClickingOutside = false,
                        )
                        if (!downloadDialogResult) {
                            cancelled = true
                            ocrRepo.cancelDownloadingTessData()
                        }
                    }
                    try {
                        if (ocrRepo.downloadTessData(ocrLang.innerCode)) {
                            dialogJob.cancel()
                            TextRecognizer.invalidSupportLanguages()
                            return true
                        } else {
                            if (!cancelled) {
                                context.showErrorDialog("Download OCR model failed with unknown error")
                            }
                        }
                    } catch (e: Exception) {
                        if (!cancelled) {
                            context.showErrorDialog("Download OCR model failed: ${e.message ?: e.localizedMessage}")
                        }
                    }
                }
                else -> {
                    logger.warn("The recognizer [$recognizer] does not implement the model downloader")
                }
            }
        }

        return false
    }

    fun onTranslationProviderClicked() {
        viewScope.launch {
            _displayTranslationProviders.value = translationRepo.getAllProviders().first()
        }
    }

    fun onTranslationProviderSelected(key: String) {
        viewScope.launch {
            val selectedProvider = translationRepo.setSelectedProvider(key).first()
            _selectedTranslationProviderName.value = selectedProvider.displayName
            loadTranslationLanguageList(selectedProvider.key)
        }
    }

    fun onTranslationLangChecked(langCode: String) {
        viewScope.launch {
            translationRepo.setSelectedTranslationLang(langCode)
            val translationLangList = _translationLangList.value ?: return@launch
            _translationLangList.value = translationLangList.map {
                it.copy(selected = it.code == langCode)
            }
        }
    }
}

data class LangItem(
    val code: String,
    val displayName: String,
    val selected: Boolean,
    val showDownloadIcon: Boolean = false,
)
