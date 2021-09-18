package tw.firemaples.onscreenocr.pref

import com.chibatching.kotpref.KotprefModel
import tw.firemaples.onscreenocr.utils.Constraints

object AppPref : KotprefModel() {
    var hasNotch: Boolean by booleanPref(default = false)
    var notchHeight: Int by intPref(default = 0)
    var statusBarHeight: Int by intPref(default = 0)

    var selectedOCRLang by stringPref(default = Constraints.DEFAULT_OCR_LANG)

    var selectedTranslationProvider by stringPref(
        default = Constraints.DEFAULT_TRANSLATION_PROVIDER.key
    )
    var selectedTranslationLang by stringPref(default = Constraints.DEFAULT_TRANSLATION_LANG)
}
