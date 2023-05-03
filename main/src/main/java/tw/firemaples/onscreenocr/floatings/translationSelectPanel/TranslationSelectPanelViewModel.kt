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
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.RecognitionLanguage
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

    private val _ocrLanguageList = MutableLiveData<Pair<List<OCRLangItem>, Boolean>>()
    val ocrLanguageList: LiveData<Pair<List<OCRLangItem>, Boolean>> = _ocrLanguageList

    private val _selectedTranslationProviderName = MutableLiveData<String>()
    val selectedTranslationProviderName: LiveData<String> = _selectedTranslationProviderName

    private val _displayTranslationProviders = MutableLiveData<List<TranslationProvider>>()
    val displayTranslateProviders: LiveData<List<TranslationProvider>> =
        _displayTranslationProviders

    private val _translationLangList = MutableLiveData<Pair<List<TranslateLangItem>, Boolean>>()
    val translationLangList: LiveData<Pair<List<TranslateLangItem>, Boolean>> = _translationLangList

    private val _displayTranslationHint = MutableLiveData<String?>()
    val displayTranslationHint: LiveData<String?> = _displayTranslationHint

    private val logger: Logger by lazy { Logger(TranslationSelectPanelViewModel::class) }
    private val context: Context by lazy { Utils.context }

    private val ocrRepo = OCRRepository()
    private val translationRepo = TranslationRepository()

    fun load() {
        viewScope.launch {
            loadOCRLanguageList(true)

            val selectedTranslationProvider =
                translationRepo.getSelectedProvider().first()
            _selectedTranslationProviderName.value = selectedTranslationProvider.displayName

            loadTranslationLanguageList(
                providerKey = selectedTranslationProvider.key,
                scrollToPosition = true,
            )
        }
    }

    private suspend fun loadOCRLanguageList(scrollToPosition: Boolean) {
        val favorites = withContext(Dispatchers.IO) { AppPref.favoriteOCRLang }
        val ocrLanguages = ocrRepo.getAllOCRLanguages().first()
        val list = ocrLanguages
            .map {
                OCRLangItem(
                    code = it.code,
                    displayName = it.displayName,
                    selected = it.selected,
                    showDownloadIcon = !it.downloaded,
                    unrecommended = it.unrecommended,
                    favorite = favorites.contains(it.code),
                    ocrLang = it,
                )
            }
        val favoriteList = list.filter { it.favorite }
        _ocrLanguageList.value = favoriteList + list to scrollToPosition
    }

    private suspend fun loadTranslationLanguageList(
        providerKey: String,
        scrollToPosition: Boolean,
    ) {
        val translationLanguages =
            translationRepo.getTranslationLanguageList(providerKey).first()

        if (translationLanguages.isEmpty()) {
            _displayTranslationHint.value = translationRepo.getTranslationHint(providerKey).first()
            _translationLangList.value = emptyList<TranslateLangItem>() to false
        } else {
            val selectedOCRLang = ocrRepo.selectedOCRLangFlow.first()

            if (translationLanguages.any {
                    it.code.firstPart() == selectedOCRLang.firstPart()
                }) {
                val favorites = AppPref.favoriteTranslationLang
                _displayTranslationHint.value = null
                val list = translationLanguages
                    .map {
                        TranslateLangItem(
                            code = it.code,
                            displayName = it.displayName,
                            selected = it.selected,
                            favorite = favorites.contains(it.code),
                        )
                    }
                val favoriteList = list.filter { it.favorite }
                _translationLangList.value = favoriteList + list to scrollToPosition
            } else {
                _displayTranslationHint.value =
                    context.getString(R.string.msg_translator_provider_does_not_support_the_ocr_lang)
                _translationLangList.value = emptyList<TranslateLangItem>() to scrollToPosition
            }
        }
    }

    fun onOCRLangSelected(langItem: OCRLangItem) {
        viewScope.launch {
            if (langItem.showDownloadIcon) {
                if (!downloadOCRModel(langItem)) {
                    return@launch
                }

                logger.debug("Download lang item success: $langItem")
            }

            ocrRepo.setSelectedOCRLanguage(langItem.code, langItem.recognizer)
            val ocrLangList = _ocrLanguageList.value ?: return@launch
            _ocrLanguageList.value = ocrLangList.first.map {
                when {
                    it.code == langItem.code && it.recognizer == langItem.recognizer ->
                        it.copy(selected = true, showDownloadIcon = false)

                    it.selected ->
                        it.copy(selected = false)

                    else -> it
                }
            } to true

            loadTranslationLanguageList(
                providerKey = translationRepo.selectedProviderTypeFlow.first().key,
                scrollToPosition = true,
            )
        }
    }

    fun onOCRLangLongClicked(langCode: String) {
        viewScope.launch {
            val favorites = AppPref.favoriteOCRLang
            if (favorites.contains(langCode)) {
                favorites.remove(langCode)
            } else {
                favorites.add(langCode)
            }
            loadOCRLanguageList(false)
        }
    }

    private suspend fun downloadOCRModel(langItem: OCRLangItem): Boolean {
        val ocrLang = langItem.ocrLang
        val result = context.showDialog(
            title = "Download OCR model",
            message = "Do you want to download ${langItem.displayName} language model for OCR?",
            dialogType = DialogView.DialogType.CONFIRM_CANCEL,
            cancelByClickingOutside = true,
        )

        if (result) {
            when (val recognizer = ocrLang.recognizer) {
                TextRecognitionProviderType.Tesseract -> {
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
            loadTranslationLanguageList(
                providerKey = selectedProvider.key,
                scrollToPosition = true,
            )
        }
    }

    fun onTranslationLangChecked(langCode: String) {
        viewScope.launch {
            translationRepo.setSelectedTranslationLang(langCode)
            val translationLangList = _translationLangList.value ?: return@launch
            _translationLangList.value = translationLangList.first.map {
                it.copy(selected = it.code == langCode)
            } to true
        }
    }

    fun onTranslationLangLongClicked(langCode: String) {
        viewScope.launch {
            val favorites = AppPref.favoriteTranslationLang
            if (favorites.contains(langCode)) {
                favorites.remove(langCode)
            } else {
                favorites.add(langCode)
            }

            val selectedTranslationProvider =
                translationRepo.getSelectedProvider().first()

            loadTranslationLanguageList(
                providerKey = selectedTranslationProvider.key,
                scrollToPosition = false,
            )
        }
    }
}

sealed class LangItem(
    open val code: String,
    open val displayName: String,
    open val selected: Boolean,
    open val showDownloadIcon: Boolean = false,
    open val unrecommended: Boolean = false,
    open val favorite: Boolean,
)

data class OCRLangItem(
    override val code: String,
    override val displayName: String,
    override val selected: Boolean,
    override val showDownloadIcon: Boolean = false,
    override val unrecommended: Boolean = false,
    override val favorite: Boolean,
    val ocrLang: RecognitionLanguage,
) : LangItem(
    code = code,
    displayName = displayName,
    selected = selected,
    showDownloadIcon = showDownloadIcon,
    unrecommended = unrecommended,
    favorite = favorite,
) {
    val recognizer: TextRecognitionProviderType get() = ocrLang.recognizer
}

data class TranslateLangItem(
    override val code: String,
    override val displayName: String,
    override val selected: Boolean,
    override val favorite: Boolean,
) : LangItem(
    code = code,
    displayName = displayName,
    selected = selected,
    favorite = favorite,
)
