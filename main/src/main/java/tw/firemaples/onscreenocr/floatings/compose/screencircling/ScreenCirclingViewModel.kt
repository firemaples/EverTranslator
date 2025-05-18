package tw.firemaples.onscreenocr.floatings.compose.screencircling

import android.graphics.Rect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.data.usecase.GetCurrentOCRLangUseCase
import tw.firemaples.onscreenocr.data.usecase.GetLastSelectedAreaUseCase
import tw.firemaples.onscreenocr.data.usecase.GetRememberLastSelectionAreaUseCase
import tw.firemaples.onscreenocr.data.usecase.SetLastSelectedAreaUseCase
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.manager.NavigationAction
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject

interface ScreenCirclingViewModel {
    val state: StateFlow<ScreenCirclingState>
    fun onViewDisplayed()
    fun onTranslateClicked()
    fun onCloseClick()
    fun onCirclingViewPrepared(viewRect: Rect)
    fun onAreaSelected(selected: Rect)
}

data class ScreenCirclingState(
    val selectedArea: Rect? = null,
)

class ScreenCirclingViewModelImpl @Inject constructor(
    @MainImmediateCoroutineScope
    private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
    private val getRememberLastSelectionAreaUseCase: GetRememberLastSelectionAreaUseCase,
    private val getCurrentOCRLangUseCase: GetCurrentOCRLangUseCase,
    private val getLastSelectedAreaUseCase: GetLastSelectedAreaUseCase,
    private val setLastSelectedAreaUseCase: SetLastSelectedAreaUseCase,
) : ScreenCirclingViewModel {
    private val logger = Logger(ScreenCirclingViewModel::class)

    override val state = MutableStateFlow(ScreenCirclingState())

    private var viewRectFlow = MutableSharedFlow<Rect>()
    private var selectedRectFlow = MutableSharedFlow<Rect>()

    init {
        combine(
            flow = viewRectFlow,
            flow2 = selectedRectFlow,
            transform = ::onScreenCircled,
        ).launchIn(scope)
    }

    override fun onViewDisplayed() {
        logger.debug("onViewDisplayed()")
        if (getRememberLastSelectionAreaUseCase.invoke()) {
            val lastSelectedArea = getLastSelectedAreaUseCase.invoke()
            state.update {
                it.copy(
                    selectedArea = lastSelectedArea,
                )
            }

            if (lastSelectedArea != null) {
                scope.launch {
                    selectedRectFlow.emit(lastSelectedArea)
                }
            }
        }
    }

    override fun onTranslateClicked() {
        scope.launch {
            val (ocrProvider, ocrLang) = getCurrentOCRLangUseCase.invoke().first()
            stateNavigator.navigate(
                NavigationAction.NavigateToScreenCapturing(
                    ocrLang = ocrLang,
                    ocrProvider = ocrProvider,
                )
            )
        }
    }

    override fun onCloseClick() {
        scope.launch {
            stateNavigator.navigate(NavigationAction.CancelScreenCircling)
        }
    }

    override fun onCirclingViewPrepared(viewRect: Rect) {
        logger.debug("onCirclingViewPrepared(), $viewRect")
        scope.launch {
            viewRectFlow.emit(viewRect)
        }
    }

    override fun onAreaSelected(selected: Rect) {
        logger.debug("onAreaSelected(), $selected")
        scope.launch {
            setLastSelectedAreaUseCase.invoke(selected)
            selectedRectFlow.emit(selected)
            state.update {
                it.copy(selectedArea = selected)
            }
        }
    }

    private fun onScreenCircled(viewRect: Rect, selectedRect: Rect) {
        logger.debug("onScreenCircled(), parent: $viewRect, selected: $selectedRect")
        scope.launch {
            stateNavigator.navigate(
                NavigationAction.NavigateToScreenCircled(
                    parentRect = viewRect,
                    selectedRect = selectedRect,
                )
            )
        }
    }
}
