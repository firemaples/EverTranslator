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
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject

interface MainBarViewModel {
    val state: StateFlow<MainBarState>
    val action: SharedFlow<MainBarAction>
    fun getInitialPosition(): Point
    fun onMenuItemClicked(key: String)
    fun onSelectClicked()
    fun onTranslateClicked()
    fun onCloseClicked()
    fun onMenuButtonClicked()
    fun onAttachedToScreen()
    fun saveLastPosition(x: Int, y: Int)
    fun onLanguageBlockClicked()
}

data class MainBarState(
    val langText: String = "",
    val translatorIcon: Int? = null,
    val displaySelectButton: Boolean = false,
    val displayTranslateButton: Boolean = false,
    val displayCloseButton: Boolean = false,
)

sealed interface MainBarAction {
    data object RescheduleFadeOut : MainBarAction
}

@Suppress("LongParameterList", "TooManyFunctions")
class MainBarViewModelImpl @Inject constructor(
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    stateNavigator: StateNavigator,
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
        action.emit(MainBarAction.RescheduleFadeOut)
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

    private fun updateLanguageStates(
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
    }

    override fun getInitialPosition(): Point =
        getMainBarInitialPositionUseCase.invoke()

    override fun onMenuItemClicked(key: String) {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onSelectClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onTranslateClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onCloseClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onMenuButtonClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun onAttachedToScreen() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }

    override fun saveLastPosition(x: Int, y: Int) {
        scope.launch {
            saveLastMainBarPositionUseCase.invoke(x = x, y = y)
        }
    }

    override fun onLanguageBlockClicked() {
        scope.launch {
            action.emit(MainBarAction.RescheduleFadeOut)
        }
    }
}
