package tw.firemaples.onscreenocr.data.repo

import android.graphics.Point
import android.graphics.Rect
import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import tw.firemaples.onscreenocr.pref.AppPref
import javax.inject.Inject

class PreferenceRepository @Inject constructor() {
    fun saveLastMainBarPosition(x: Int, y: Int) {
        AppPref.lastMainBarPosition = Point(x, y)
    }

    fun getLastMainBarPosition(): Point =
        AppPref.lastMainBarPosition

    fun getShowTextSelectionOnResultView() =
        AppPref.asLiveData(AppPref::displaySelectedTextOnResultWindow).asFlow()

    fun setShowTextSelectionOnResultView(show: Boolean) {
        AppPref.displaySelectedTextOnResultWindow = show
    }

    fun getResultViewFontSize() =
        AppPref.asLiveData(AppPref::resultWindowFontSize).asFlow()

    fun setResultViewFontSize(fontSize: Float) {
        AppPref.resultWindowFontSize = fontSize
    }

    fun getLastSelectedArea() = AppPref.lastSelectionArea

    fun setLastSelectedArea(rect: Rect) {
        AppPref.lastSelectionArea = rect
    }
}
