package tw.firemaples.onscreenocr.pref

import android.graphics.Rect
import com.chibatching.kotpref.Kotpref
import com.chibatching.kotpref.KotprefModel
import com.chibatching.kotpref.gsonpref.gson
import com.chibatching.kotpref.gsonpref.gsonNullablePref
import com.google.gson.Gson
import tw.firemaples.onscreenocr.utils.Constants

object AppPref : KotprefModel() {
    init {
        Kotpref.gson = Gson()
    }

    var hasNotch: Boolean by booleanPref(default = false)
    var notchHeight: Int by intPref(default = 0)
    var statusBarHeight: Int by intPref(default = 0)

    var selectedOCRLang by stringPref(default = Constants.DEFAULT_OCR_LANG)

    var selectedTranslationProvider by stringPref(
        default = Constants.DEFAULT_TRANSLATION_PROVIDER.key
    )
    var selectedTranslationLang by stringPref(default = Constants.DEFAULT_TRANSLATION_LANG)

    var lastSelectionArea: Rect? by gsonNullablePref()

    var lastVersionHistoryShownVersion: String? by nullableStringPref(default = null)
    var lastReadmeShownVersion: String? by nullableStringPref(default = null)
}
