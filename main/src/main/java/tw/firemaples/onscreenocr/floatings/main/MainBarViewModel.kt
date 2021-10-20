package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.floatings.manager.FloatingStateManager
import tw.firemaples.onscreenocr.floatings.manager.State
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.repo.GeneralRepository
import tw.firemaples.onscreenocr.repo.OCRRepository
import tw.firemaples.onscreenocr.repo.TranslationRepository
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.SingleLiveEvent
import tw.firemaples.onscreenocr.utils.Utils

class MainBarViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    companion object {
        private const val MENU_SETTING = "setting"
        private const val MENU_PRIVACY_POLICY = "privacy_policy"
        private const val MENU_ABOUT = "about"
        private const val MENU_VERSION_HISTORY = "version_history"
        private const val MENU_README = "readme"
        private const val MENU_HIDE = "hide"
        private const val MENU_EXIT = "exit"
    }

    private val _languageText = MutableLiveData<String>()
    val languageText: LiveData<String> = _languageText

    private val _displayGoogleTranslateIcon = MutableLiveData<Boolean>()
    val displayGoogleTranslateIcon: LiveData<Boolean> = _displayGoogleTranslateIcon

    private val _displaySelectButton = MutableLiveData<Boolean>()
    val displaySelectButton: LiveData<Boolean> = _displaySelectButton

    private val _displayTranslateButton = MutableLiveData<Boolean>()
    val displayTranslateButton: LiveData<Boolean> = _displayTranslateButton

    private val _displayCloseButton = MutableLiveData<Boolean>()
    val displayCloseButton: LiveData<Boolean> = _displayCloseButton

    private val _displayMenuItems = MutableLiveData<Map<String, String>>()
    val displayMenuItems: LiveData<Map<String, String>> = _displayMenuItems

    private val _rescheduleFadeOut = MutableLiveData<Boolean>()
    val rescheduleFadeOut: LiveData<Boolean> = _rescheduleFadeOut

    private val _showSettingPage = SingleLiveEvent<Boolean>()
    val showSettingPage: LiveData<Boolean> = _showSettingPage

    private val _openBrowser = SingleLiveEvent<String>()
    val openBrowser: LiveData<String> = _openBrowser

    private val _showVersionHistory = SingleLiveEvent<Boolean>()
    val showVersionHistory: LiveData<Boolean> = _showVersionHistory

    private val _showReadme = SingleLiveEvent<Boolean>()
    val showReadme: LiveData<Boolean> = _showReadme

    private val logger: Logger by lazy { Logger(MainBarViewModel::class) }
    private val context: Context by lazy { Utils.context }

    private val menuItems = mapOf(
        MENU_SETTING to "Setting",
        MENU_PRIVACY_POLICY to "Privacy Policy",
        MENU_ABOUT to "About",
        MENU_VERSION_HISTORY to "Version History",
        MENU_README to "Readme",
        MENU_HIDE to "Hide",
        MENU_EXIT to "Exit",
    )

    private val repo by lazy { GeneralRepository() }
    private val ocrRepo by lazy { OCRRepository() }
    private val translateRepo by lazy { TranslationRepository() }

    private var selectedOCRLang: String = Constants.DEFAULT_OCR_LANG

    //    private var selectedTranslationProvider: TranslationProvider =
//        TranslationProvider.fromType(context, Constraints.DEFAULT_TRANSLATION_PROVIDER)
    private var selectedTranslationProviderType: TranslationProviderType =
        Constants.DEFAULT_TRANSLATION_PROVIDER
    private var selectedTranslationLang: String = Constants.DEFAULT_TRANSLATION_LANG

    fun onAttachedToScreen() {
        logger.debug("onAttachedToScreen()")
        viewScope.launch {
            logger.debug("register FloatingStateManager.onStateChanged")
            FloatingStateManager.currentStateFlow.collect { onStateChanged(it) }
        }
        viewScope.launch {
            ocrRepo.selectedOCRLangFlow.collect { onSelectedLangChanged(_ocrLang = it) }
        }
        viewScope.launch {
            translateRepo.selectedProviderTypeFlow.collect {
                onSelectedLangChanged(translationProviderType = it)
            }
        }
        viewScope.launch {
            translateRepo.selectedTranslationLangFlow.collect {
                onSelectedLangChanged(translationLang = it)
            }
        }
        viewScope.launch {
            setupButtons(FloatingStateManager.currentState)

            if (!repo.isReadmeAlreadyShown().first()) {
                _showReadme.value = true
            }

            if (!repo.isVersionHistoryAlreadyShown().first()) {
                _showVersionHistory.value = true
            }
        }
    }

    private suspend fun onStateChanged(state: State) {
        logger.debug("onStateChanged(): $state")
        setupButtons(state)
        _rescheduleFadeOut.value = true
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun setupButtons(state: State) {
        logger.debug("setupButtons(): $state")
        _displaySelectButton.value = state == State.Idle
        _displayTranslateButton.value = state == State.ScreenCircled
        _displayCloseButton.value =
            state == State.ScreenCircling || state == State.ScreenCircled
    }

    @Suppress("RedundantSuspendModifier")
    private suspend fun onSelectedLangChanged(
        _ocrLang: String = selectedOCRLang,
        translationProviderType: TranslationProviderType = selectedTranslationProviderType,
        translationLang: String = selectedTranslationLang,
    ) {
        this.selectedOCRLang = _ocrLang
        this.selectedTranslationProviderType = translationProviderType
        this.selectedTranslationLang = translationLang

        logger.debug("onSelectedLangChanged(), ocrLang: $_ocrLang, provider: $translationProviderType, translationLang: $translationLang")

        val ocrLang = TextRecognizer.getRecognizer().parseToDisplayLangCode(_ocrLang)

        val displayGoogleTranslateIcon =
            translationProviderType == TranslationProviderType.GoogleTranslateApp

        val langText = when (translationProviderType) {
            TranslationProviderType.GoogleTranslateApp -> "$ocrLang>"
            TranslationProviderType.OCROnly -> " $ocrLang "
            else -> "$ocrLang>$translationLang"
        }

        _displayGoogleTranslateIcon.value = displayGoogleTranslateIcon
        _languageText.value = langText
    }

    fun onMenuButtonClicked() {
        viewScope.launch {
            _rescheduleFadeOut.value = true
            _displayMenuItems.value = menuItems
        }
    }

    fun onMenuItemClicked(action: String) {
        logger.debug("onMenuItemClicked(), action: $action")

        when (action) {
            MENU_SETTING -> {
                _showSettingPage.value = true
            }
            MENU_PRIVACY_POLICY -> {
                _openBrowser.value = "https://sites.google.com/view/privacy-policy-evertranslator"
            }
            MENU_ABOUT -> {
                _openBrowser.value = "https://github.com/firemaples/EverTranslator"
            }
            MENU_VERSION_HISTORY -> {
                _showVersionHistory.value = true
            }
            MENU_README -> {
                _showReadme.value = true
            }
            MENU_HIDE -> {
                ViewHolderService.hideViews(context)
            }
            MENU_EXIT -> {
                ViewHolderService.exit(context)
            }
        }
    }
}