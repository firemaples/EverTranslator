package tw.firemaples.onscreenocr.translator.utils

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants

object PapagoTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_PAPAGO_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.PapagoTranslateApp

    override fun launch(context: Context, text: String) {
        val i = Intent()
        i.action = Intent.ACTION_PROCESS_TEXT
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.component = ComponentName("com.naver.labs.translator", "com.naver.labs.translator.ui.mini.control.ServiceStartActivity")
        i.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        i.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

        context.startActivity(i)
    }
}
