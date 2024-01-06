package tw.firemaples.onscreenocr.floatings.manager

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.di.MainImmediateCoroutineScope
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.compose.mainbar.MainBarFloatingView
import tw.firemaples.onscreenocr.floatings.dialog.showErrorDialog
import tw.firemaples.onscreenocr.floatings.result.ResultView
import tw.firemaples.onscreenocr.floatings.screenCircling.ScreenCirclingView
import tw.firemaples.onscreenocr.utils.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FloatingViewCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    @MainImmediateCoroutineScope private val scope: CoroutineScope,
    private val stateNavigator: StateNavigator,
    stateOperator: StateOperator,
    private val mainBar: MainBarFloatingView,
    private val resultView: ResultView,
) {
    private val logger: Logger by lazy { Logger(FloatingViewCoordinator::class) }

    private val screenCirclingView: ScreenCirclingView by lazy {
        ScreenCirclingView(context).apply {
            onAreaSelected = { parent, selected ->
                scope.launch {
                    stateNavigator.navigate(
                        NavigationAction.NavigateToScreenCircled(
                            parentRect = parent,
                            selectedRect = selected,
                        )
                    )
                }
            }
        }
    }

    val showingStateChangedFlow = MutableStateFlow(false)
    val isMainBarAttached: Boolean
        get() = mainBar.attached

    init {
        stateOperator.action
            .onEach { action ->
                when (action) {
                    StateOperatorAction.TopMainBar -> arrangeMainBarToTop()
                    StateOperatorAction.HideMainBar -> hideMainBar()
                    StateOperatorAction.ShowMainBar -> showMainBar()

                    StateOperatorAction.ShowScreenCirclingView ->
                        screenCirclingView.attachToScreen()

                    StateOperatorAction.HideScreenCirclingView ->
                        screenCirclingView.detachFromScreen()

                    StateOperatorAction.ResultViewStartRecognition ->
                        resultView.startRecognition()

                    is StateOperatorAction.ResultViewSetRecognized ->
                        resultView.textRecognized(
                            result = action.result,
                            parent = action.parentRect,
                            selected = action.selectedRect,
                            croppedBitmap = action.croppedBitmap,
                        )

                    is StateOperatorAction.ResultViewStartTranslation ->
                        resultView.startTranslation(action.translationProviderType)

                    is StateOperatorAction.ResultViewTextTranslated ->
                        resultView.textTranslated(action.result)

                    StateOperatorAction.ResultViewBackToIdle ->
                        resultView.backToIdle()

                    is StateOperatorAction.ShowErrorDialog ->
                        context.showErrorDialog(action.error)
                }
            }
            .launchIn(scope)

        resultView.onUserDismiss = {
            scope.launch {
                resultView.backToIdle()
                stateNavigator.navigate(NavigationAction.NavigateToIdle(showMainBar = true))
            }
        }
    }

    fun showMainBar() {
        if (isMainBarAttached) return
        mainBar.attachToScreen()
        scope.launch {
            showingStateChangedFlow.emit(true)
        }
    }

    private fun hideMainBar() {
        if (!isMainBarAttached) return
        mainBar.detachFromScreen()
        scope.launch {
            showingStateChangedFlow.emit(false)
        }
    }

    private fun arrangeMainBarToTop() {
        mainBar.detachFromScreen()
        mainBar.attachToScreen()
    }

    fun detachAllViews() {
        scope.launch {
            stateNavigator.navigate(NavigationAction.NavigateToIdle(showMainBar = false))
            hideMainBar()
            FloatingView.detachAllFloatingViews()
        }
    }
}
