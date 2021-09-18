package tw.firemaples.onscreenocr.floatings.translationSelectPanel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.repo.OCRRepository
import tw.firemaples.onscreenocr.repo.TranslationRepository
import tw.firemaples.onscreenocr.translator.TranslationProvider

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
            _displayTranslationHint.value = null
            _translationLangList.value = translationLanguages
                .map { LangItem(it.code, it.displayName, it.selected) }
        }
    }

    fun onOCRLangSelected(langCode: String) {
        viewScope.launch {
            ocrRepo.setSelectedOCRLanguage(langCode)
            val ocrLangList = _ocrLanguageList.value ?: return@launch
            _ocrLanguageList.value = ocrLangList.map {
                it.copy(selected = it.code == langCode)
            }
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
