package tw.firemaples.onscreenocr.floatings.compose.mainbar

import android.graphics.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.data.usecase.GetCurrentOCRDisplayLangCodeUseCase
import tw.firemaples.onscreenocr.data.usecase.GetCurrentTranslationLangUseCase
import tw.firemaples.onscreenocr.data.usecase.GetCurrentTranslatorTypeUseCase
import tw.firemaples.onscreenocr.data.usecase.GetMainBarInitialPositionUseCase
import tw.firemaples.onscreenocr.data.usecase.SaveLastMainBarPositionUseCase
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.compose.base.awaitForSubscriber
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject

interface MainBarViewModel {
    val state: StateFlow<MainBarState>
    val action: SharedFlow<MainBarAction>
    fun getInitialPosition(): Point
    fun getFadeOutAfterMoved(): Boolean
    fun getFadeOutDelay(): Long
    fun getFadeOutDestinationAlpha(): Float
    fun onMenuItemClicked(key: String)
    fun onSelectClicked()
    fun onTranslateClicked()
    fun onCloseClicked()
    fun onMenuButtonClicked()
    fun onAttachedToScreen()
    fun onDragEnd(x: Int, y: Int)
    fun onLanguageBlockClicked()
    fun onMenuOptionSelected(mainBarMenuOption: MainBarMenuOption?)
}

data class MainBarState(
    val langText: String = "",
    val translatorIcon: Int? = null,
    val displaySelectButton: Boolean = false,
    val displayTranslateButton: Boolean = false,
    val displayCloseButton: Boolean = false,
    val displayMainBarMenu: Boolean = false,
)

sealed interface MainBarAction {
    data object RescheduleFadeOut : MainBarAction
    data object MoveToEdgeIfEnabled : MainBarAction
    data object OpenLanguageSelectionPanel : MainBarAction
    data object OpenSettings : MainBarAction
    data class OpenBrowser(val url: String) : MainBarAction
    data object OpenVersionHistory : MainBarAction
    data object OpenReadme : MainBarAction
    data object HideMainBar : MainBarAction
    data object ExitApp : MainBarAction
}

@Suppress("LongParameterList", "TooManyFunctions")
class MainBarViewModelImpl @Inject constructor(
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
    private val getCurrentOCRDisplayLangCodeUseCase: GetCurrentOCRDisplayLangCodeUseCase,
    private val getCurrentTranslatorTypeUseCase: GetCurrentTranslatorTypeUseCase,
    private val getCurrentTranslationLangUseCase: GetCurrentTranslationLangUseCase,
    private val saveLastMainBarPositionUseCase: SaveLastMainBarPositionUseCase,
    private val getMainBarInitialPositionUseCase: GetMainBarInitialPositionUseCase,
) : MainBarViewModel {
    override val state = MutableStateFlow(MainBarState())
    override val action = MutableSharedFlow<MainBarAction>()

    private val logger: Logger by lazy { Logger(this::class) }

    init {
        stateNavigator.currentNavState
            .onEach { onNavigationStateChanges(it) }
            .launchIn(scope)
        subscribeLanguageStateChanges()
    }

    private suspend fun onNavigationStateChanges(navState: NavState) {
        state.update {
            it.copy(
                displaySelectButton = navState == NavState.Idle,
                displayTranslateButton = navState == NavState.ScreenCircled,
                displayCloseButton =
                navState == NavState.ScreenCircling || navState == NavState.ScreenCircled,
            )
        }
        action.emit(MainBarAction.MoveToEdgeIfEnabled)
    }

    private fun subscribeLanguageStateChanges() {
        combine(
            getCurrentOCRDisplayLangCodeUseCase.invoke(),
            getCurrentTranslatorTypeUseCase.invoke(),
            getCurrentTranslationLangUseCase.invoke(),
        ) { ocrLang, translatorType, translationLang ->
            updateLanguageStates(
                ocrLang = ocrLang,
                translationProviderType = translatorType,
                translationLang = translationLang,
            )
        }.launchIn(scope)
    }

    private suspend fun updateLanguageStates(
        ocrLang: String,
        translationProviderType: TranslationProviderType,
        translationLang: String,
    ) {
        val icon = when (translationProviderType) {
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

        val text = when (translationProviderType) {
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

        state.update {
            it.copy(
                langText = text,
                translatorIcon = icon,
            )
        }
        action.emit(MainBarAction.MoveToEdgeIfEnabled)
    }

    override fun getInitialPosition(): Point =
        getMainBarInitialPositionUseCase.invoke()

    override fun getFadeOutAfterMoved(): Boolean {
        val navState = stateNavigator.currentNavState.value

        return navState != NavState.ScreenCircling && navState != NavState.ScreenCircled
                && !state.value.displayMainBarMenu
                && SettingManager.enableFadingOutWhileIdle //TODO move logic
    }

    override fun getFadeOutDelay(): Long =
        SettingManager.timeoutToFadeOut //TODO move logic

    override fun getFadeOutDestinationAlpha(): Float =
        SettingManager.opaquePercentageToFadeOut //TODO move logic

    override fun onMenuItemClicked(key: String) {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onSelectClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
            stateNavigator.navigate(NavigationAction.NavigateToScreenCircling)
        }
    }

    override fun onTranslateClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
            stateNavigator.navigate(NavigationAction.NavigateToScreenCapturing)
        }
    }

    override fun onCloseClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
            stateNavigator.navigate(NavigationAction.CancelScreenCircling)
        }
    }

    override fun onMenuButtonClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
            state.update {
                it.copy(
                    displayMainBarMenu = true,
                )
            }
        }
    }

    override fun onAttachedToScreen() {
        scope.launch {
            action.awaitForSubscriber()
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onDragEnd(x: Int, y: Int) {
        scope.launch {
            saveLastMainBarPositionUseCase.invoke(x = x, y = y)
        }
    }

    override fun onLanguageBlockClicked() {
        scope.launch {
            action.emit(MainBarAction.OpenLanguageSelectionPanel)
        }
    }

    override fun onMenuOptionSelected(mainBarMenuOption: MainBarMenuOption?) {
        scope.launch {
            state.update {
                it.copy(
                    displayMainBarMenu = false,
                )
            }

            when (mainBarMenuOption) {
                MainBarMenuOption.SETTING ->
                    action.emit(MainBarAction.OpenSettings)

                MainBarMenuOption.PRIVACY_POLICY ->
                    action.emit(MainBarAction.OpenBrowser(RemoteConfigManager.privacyPolicyUrl))

                MainBarMenuOption.ABOUT ->
                    action.emit(MainBarAction.OpenBrowser(RemoteConfigManager.aboutUrl))

                MainBarMenuOption.VERSION_HISTORY ->
                    action.emit(MainBarAction.OpenVersionHistory)

                MainBarMenuOption.README ->
                    action.emit(MainBarAction.OpenReadme)

                MainBarMenuOption.HIDE ->
                    action.emit(MainBarAction.HideMainBar)

                MainBarMenuOption.EXIT ->
                    action.emit(MainBarAction.ExitApp)

                null -> {}
            }
        }
    }
}
