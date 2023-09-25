package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.floatings.manager.FloatingStateManager
import tw.firemaples.onscreenocr.floatings.manager.State
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.TextRecognizer
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
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

    private val _displayTranslatorIcon = MutableLiveData<Int?>()
    val displayTranslatorIcon: LiveData<Int?> = _displayTranslatorIcon

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
        MENU_SETTING to context.getString(R.string.menu_setting),
        MENU_PRIVACY_POLICY to context.getString(R.string.menu_privacy_policy),
        MENU_ABOUT to context.getString(R.string.menu_about),
        MENU_VERSION_HISTORY to context.getString(R.string.menu_version_history),
        MENU_README to context.getString(R.string.menu_readme),
        MENU_HIDE to context.getString(R.string.menu_hide),
        MENU_EXIT to context.getString(R.string.menu_exit),
    )

    private val repo by lazy { GeneralRepository() }
    private val ocrRepo by lazy { OCRRepository() }
    private val translateRepo by lazy { TranslationRepository() }

    private var _selectedOCRLang: String = Constants.DEFAULT_OCR_LANG
    val selectedOCRLang: String get() = _selectedOCRLang

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

            if (repo.showVersionHistory().first()) {
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
        _ocrLang: String = _selectedOCRLang,
        translationProviderType: TranslationProviderType = selectedTranslationProviderType,
        translationLang: String = selectedTranslationLang,
    ) {
        this._selectedOCRLang = _ocrLang
        this.selectedTranslationProviderType = translationProviderType
        this.selectedTranslationLang = translationLang

        logger.debug("onSelectedLangChanged(), ocrLang: $_ocrLang, provider: $translationProviderType, translationLang: $translationLang")

        val ocrLang = TextRecognizer
            .getRecognizer(AppPref.selectedOCRProvider)
            .parseToDisplayLangCode(_ocrLang)

        _displayTranslatorIcon.value = when (translationProviderType) {
            TranslationProviderType.GoogleTranslateApp -> R.drawable.ic_google_translate_dark_grey
            TranslationProviderType.BingTranslateApp -> R.drawable.ic_microsoft_bing
            TranslationProviderType.OtherTranslateApp -> R.drawable.ic_open_in_app
            TranslationProviderType.MicrosoftAzure,
            TranslationProviderType.GoogleMLKit,
            TranslationProviderType.MyMemory,
            TranslationProviderType.PapagoTranslateApp,
            TranslationProviderType.YandexTranslateApp,
            TranslationProviderType.OCROnly -> null
        }

        _languageText.value = when (translationProviderType) {
            TranslationProviderType.GoogleTranslateApp,
            TranslationProviderType.BingTranslateApp,
            TranslationProviderType.OtherTranslateApp -> "$ocrLang>"

            TranslationProviderType.YandexTranslateApp -> "$ocrLang > Y"
            TranslationProviderType.PapagoTranslateApp -> "$ocrLang > P"
            TranslationProviderType.OCROnly -> " $ocrLang "
            TranslationProviderType.MicrosoftAzure,
            TranslationProviderType.GoogleMLKit,
            TranslationProviderType.MyMemory -> "$ocrLang>$translationLang"
        }
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
                _openBrowser.value = RemoteConfigManager.privacyPolicyUrl
            }

            MENU_ABOUT -> {
                _openBrowser.value = RemoteConfigManager.aboutUrl
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

    fun saveLastPosition(x: Int, y: Int) {
        viewScope.launch {
            repo.saveLastMainBarPosition(x, y)
        }
    }
}