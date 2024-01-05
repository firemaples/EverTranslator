package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import android.graphics.Point
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.FloatingMainBarBinding
import tw.firemaples.onscreenocr.floatings.base.MovableFloatingView
import tw.firemaples.onscreenocr.floatings.history.VersionHistoryView
import tw.firemaples.onscreenocr.floatings.manager.NavState
import tw.firemaples.onscreenocr.floatings.manager.StateNavigator
import tw.firemaples.onscreenocr.floatings.menu.MenuView
import tw.firemaples.onscreenocr.floatings.readme.ReadmeView
import tw.firemaples.onscreenocr.floatings.translationSelectPanel.TranslationSelectPanel
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pages.setting.SettingActivity
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.clickOnce
import tw.firemaples.onscreenocr.utils.hide
import tw.firemaples.onscreenocr.utils.show
import tw.firemaples.onscreenocr.utils.showOrHide
import javax.inject.Inject

class MainBar @Inject constructor(
    @ApplicationContext context: Context,
    private val stateNavigator: StateNavigator,
    private val viewModel: MainBarViewModel,
) : MovableFloatingView(context) {

    override val layoutId: Int
        get() = R.layout.floating_main_bar

    override val initialPosition: Point
        get() =
            if (SettingManager.restoreMainBarPosition) AppPref.lastMainBarPosition
            else Point(0, 0)

    override val enableDeviceDirectionTracker: Boolean
        get() = true

    override val moveToEdgeAfterMoved: Boolean
        get() = true

    override val fadeOutAfterMoved: Boolean
        get() = !arrayOf(NavState.ScreenCircling, NavState.ScreenCircled)
            .contains(stateNavigator.currentNavState.value)
                && !menuView.attached
                && SettingManager.enableFadingOutWhileIdle
    override val fadeOutDelay: Long
        get() = SettingManager.timeoutToFadeOut
    override val fadeOutDestinationAlpha: Float
        get() = SettingManager.opaquePercentageToFadeOut

    private val binding: FloatingMainBarBinding = FloatingMainBarBinding.bind(rootLayout)

    private val menuView: MenuView by lazy {
        MenuView(context, false).apply {
            setAnchor(binding.btMenu)

            onAttached = { rescheduleFadeOut() }
            onDetached = { rescheduleFadeOut() }
            onItemSelected = { view, key ->
                view.detachFromScreen()
                viewModel.onMenuItemClicked(key)
                rescheduleFadeOut()
            }
        }
    }

    init {
        binding.setViews()
        setDragView(binding.btMenu)
    }

    private fun FloatingMainBarBinding.setViews() {
        btLangSelector.clickOnce {
            rescheduleFadeOut()
            TranslationSelectPanel(context).attachToScreen()
        }

        btSelect.clickOnce {
            viewModel.onSelectClicked()
        }

        btTranslate.clickOnce {
            FirebaseEvent.logClickTranslationStartButton()
            viewModel.onTranslateClicked()
        }

        btClose.clickOnce {
            viewModel.onCloseClicked()
        }

        btMenu.clickOnce {
            viewModel.onMenuButtonClicked()
        }

        viewModel.languageText.observe(lifecycleOwner) {
            tvLang.text = it
            moveToEdgeIfEnabled()
        }

        viewModel.displayTranslatorIcon.observe(lifecycleOwner) {
            if (it == null) {
                ivGoogleTranslator.setImageDrawable(null)
                ivGoogleTranslator.hide()
            } else {
                ivGoogleTranslator.setImageResource(it)
                ivGoogleTranslator.show()
            }
            moveToEdgeIfEnabled()
        }

        viewModel.displaySelectButton.observe(lifecycleOwner) {
            btSelect.showOrHide(it)
            moveToEdgeIfEnabled()
        }

        viewModel.displayTranslateButton.observe(lifecycleOwner) {
            btTranslate.showOrHide(it)
            moveToEdgeIfEnabled()
        }

        viewModel.displayCloseButton.observe(lifecycleOwner) {
            btClose.showOrHide(it)
            moveToEdgeIfEnabled()
        }

        viewModel.displayMenuItems.observe(lifecycleOwner) {
            with(menuView) {
                updateData(it)
                attachToScreen()
            }
        }

        viewModel.rescheduleFadeOut.observe(lifecycleOwner) {
            rescheduleFadeOut()
        }

        viewModel.showSettingPage.observe(lifecycleOwner) {
            SettingActivity.start(context)
        }

        viewModel.openBrowser.observe(lifecycleOwner) {
            Utils.openBrowser(it)
        }

        viewModel.showVersionHistory.observe(lifecycleOwner) {
            VersionHistoryView(context).attachToScreen()
        }

        viewModel.showReadme.observe(lifecycleOwner) {
            ReadmeView(context).attachToScreen()
        }
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        viewModel.onAttachedToScreen()
    }

    override fun onDetachedFromScreen() {
        super.onDetachedFromScreen()
        viewModel.saveLastPosition(params.x, params.y)
    }
}
