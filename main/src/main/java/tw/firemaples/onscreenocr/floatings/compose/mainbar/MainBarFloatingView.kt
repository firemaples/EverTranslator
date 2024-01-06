package tw.firemaples.onscreenocr.floatings.compose.mainbar

import android.content.Context
import android.graphics.Point
import androidx.compose.runtime.Composable
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeMovableFloatingView
import tw.firemaples.onscreenocr.floatings.compose.base.collectOnLifecycleResumed
import tw.firemaples.onscreenocr.floatings.history.VersionHistoryView
import tw.firemaples.onscreenocr.floatings.readme.ReadmeView
import tw.firemaples.onscreenocr.floatings.translationSelectPanel.TranslationSelectPanel
import tw.firemaples.onscreenocr.pages.setting.SettingActivity
import tw.firemaples.onscreenocr.utils.Utils
import javax.inject.Inject

class MainBarFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: MainBarViewModel,
) : ComposeMovableFloatingView(context) {

    override val initialPosition: Point
        get() = viewModel.getInitialPosition()

    @Composable
    override fun RootContent() {
        viewModel.action.collectOnLifecycleResumed { action ->
            when (action) {
                MainBarAction.RescheduleFadeOut ->
                    rescheduleFadeOut()

                MainBarAction.MoveToEdgeIfEnabled ->
                    moveToEdgeIfEnabled()

                MainBarAction.OpenLanguageSelectionPanel -> {
                    rescheduleFadeOut()
                    // TODO wait to be refactored
                    TranslationSelectPanel(context).attachToScreen()
                }

                is MainBarAction.OpenBrowser ->
                    // TODO wait to be refactored
                    Utils.openBrowser(action.url)

                MainBarAction.OpenReadme ->
                    // TODO wait to be refactored
                    ReadmeView(context).attachToScreen()

                MainBarAction.OpenSettings ->
                    // TODO wait to be refactored
                    SettingActivity.start(context)

                MainBarAction.OpenVersionHistory ->
                    // TODO wait to be refactored
                    VersionHistoryView(context).attachToScreen()

                MainBarAction.HideMainBar ->
                    // TODO wait to be refactored
                    ViewHolderService.hideViews(context)

                MainBarAction.ExitApp ->
                    // TODO wait to be refactored
                    ViewHolderService.exit(context)
            }
        }

        MainBarContent(
            viewModel = viewModel,
            onDragStart = onDragStart,
            onDragEnd = {
                onDragEnd.invoke()
                viewModel.onDragEnd(params.x, params.y)
            },
            onDragCancel = onDragCancel,
            onDrag = onDrag,
        )
    }

    override val enableDeviceDirectionTracker: Boolean
        get() = true

    override val moveToEdgeAfterMoved: Boolean
        get() = true

    override val fadeOutAfterMoved: Boolean
        get() = viewModel.getFadeOutAfterMoved()
    override val fadeOutDelay: Long
        get() = viewModel.getFadeOutDelay()
    override val fadeOutDestinationAlpha: Float
        get() = viewModel.getFadeOutDestinationAlpha()

    override fun attachToScreen() {
        super.attachToScreen()
        viewModel.onAttachedToScreen()
    }
}
