package tw.firemaples.onscreenocr.floatings.translationSelectPanel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.repo.OCRRepository
import tw.firemaples.onscreenocr.repo.TranslationRepository
import tw.firemaples.onscreenocr.translator.TranslationProvider
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.firstPart

class TranslationSelectPanelViewModel(viewScope: CoroutineScope) :
    FloatingViewModel(viewScope) {

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

    private val context: Context by lazy { Utils.context }

    private val ocrRepo = OCRRepository()
    private val translationRepo = TranslationRepository()

    fun load() {
        viewScope.launch {
            val ocrLanguages = ocrRepo.getAllOCRLanguages().first()
            _ocrLanguageList.value = ocrLanguages
                .map { LangItem(it.code, it.displayName, it.selected) }

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

    fun onOCRLangSelected(langCode: String) {
        viewScope.launch {
            ocrRepo.setSelectedOCRLanguage(langCode)
            val ocrLangList = _ocrLanguageList.value ?: return@launch
            _ocrLanguageList.value = ocrLangList.map {
                it.copy(selected = it.code == langCode)
            }

            loadTranslationLanguageList(translationRepo.selectedProviderTypeFlow.first().key)
        }
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

data class LangItem(val code: String, val displayName: String, val selected: Boolean)
