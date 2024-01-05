package tw.firemaples.onscreenocr.data.repo

import android.graphics.Point
import tw.firemaples.onscreenocr.pref.AppPref
import javax.inject.Inject

class PreferenceRepository @Inject constructor() {
    fun saveLastMainBarPosition(x: Int, y: Int) {
        AppPref.lastMainBarPosition = Point(x, y)
    }

    fun getLastMainBarPosition(): Point =
        AppPref.lastMainBarPosition
}
