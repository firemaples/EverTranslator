package tw.firemaples.onscreenocr.data.repo

import tw.firemaples.onscreenocr.pages.setting.SettingManager
import javax.inject.Inject

class SettingRepository @Inject constructor() {
    fun shouldRestoreMainBarPosition(): Boolean =
        SettingManager.restoreMainBarPosition
}
