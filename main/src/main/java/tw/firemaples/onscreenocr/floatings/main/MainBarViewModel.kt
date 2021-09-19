package tw.firemaples.onscreenocr.floatings.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.ViewHolderService
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

class MainBarViewModel(viewScope: CoroutineScope) : FloatingViewModel(viewScope) {
    companion object {
        private const val MENU_SETTING = "setting"
        private const val MENU_PRIVACY_POLICY = "privacy_policy"
        private const val MENU_ABOUT = "about"
        private const val MENU_README = "readme"
        private const val MENU_HIDE = "hide"
        private const val MENU_EXIT = "exit"
    }

    private var _displayMenuItems = MutableLiveData<Map<String, String>>()
    val displayMenuItems: LiveData<Map<String, String>> = _displayMenuItems

    private val logger: Logger by lazy { Logger(MainBarViewModel::class) }
    private val context: Context by lazy { Utils.context }

    private val menuItems = mapOf(
        MENU_SETTING to "Setting",
        MENU_PRIVACY_POLICY to "Privacy Policy",
        MENU_ABOUT to "About",
        MENU_README to "Readme",
        MENU_HIDE to "Hide",
        MENU_EXIT to "Exit",
    )

    fun onMenuButtonClicked() {
        viewScope.launch {
            _displayMenuItems.value = menuItems
        }
    }

    fun onMenuItemClicked(action: String) {
        logger.debug("onMenuItemClicked(), action: $action")

        when (action) {
            MENU_SETTING -> {
            }
            MENU_PRIVACY_POLICY -> {
            }
            MENU_ABOUT -> {
            }
            MENU_README -> {
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