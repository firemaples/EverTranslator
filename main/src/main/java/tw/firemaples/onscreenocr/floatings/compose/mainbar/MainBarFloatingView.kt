package tw.firemaples.onscreenocr.floatings.compose.mainbar

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeMovableFloatingView
import tw.firemaples.onscreenocr.floatings.compose.base.collectOnLifecycleResumed
import tw.firemaples.onscreenocr.floatings.compose.menu.MenuFloatingView
import tw.firemaples.onscreenocr.floatings.compose.menu.MenuItem
import tw.firemaples.onscreenocr.floatings.history.VersionHistoryView
import tw.firemaples.onscreenocr.floatings.readme.ReadmeView
import tw.firemaples.onscreenocr.floatings.translationSelectPanel.TranslationSelectPanel
import tw.firemaples.onscreenocr.pages.setting.SettingActivity
import tw.firemaples.onscreenocr.utils.Utils
import javax.inject.Inject

class MainBarFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: MainBarViewModel,
    private val menuFloatingView: MenuFloatingView,
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

                MainBarAction.ShowMenu ->{
                    val anchor =
                        Rect(params.x, params.y, params.x + rootView.width, params.y + rootView.height)
                    menuFloatingView.getMenuViewDelegate().setAnchor(anchor)
                    menuFloatingView.attachToScreen()
                }

                MainBarAction.HideMenu ->
                    menuFloatingView.detachFromScreen()
            }
        }

        val menuItems: List<MenuItem> = listOf(
            MenuItem(
                key = MainBarMenuConst.MENU_SETTING,
                text = stringResource(id = R.string.menu_setting),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_PRIVACY_POLICY,
                text = stringResource(id = R.string.menu_privacy_policy),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_ABOUT,
                text = stringResource(id = R.string.menu_about),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_VERSION_HISTORY,
                text = stringResource(id = R.string.menu_version_history),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_README,
                text = stringResource(id = R.string.menu_readme),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_HIDE,
                text = stringResource(id = R.string.menu_hide),
            ),
            MenuItem(
                key = MainBarMenuConst.MENU_EXIT,
                text = stringResource(id = R.string.menu_exit),
            ),
        )
        with(menuFloatingView.getMenuViewDelegate()) {
            setMenuData(menuItems)
            setOnMenuItemClickedListener { key ->
                viewModel.onMenuItemClicked(key)
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

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        viewModel.onAttachedToScreen()
    }
}
