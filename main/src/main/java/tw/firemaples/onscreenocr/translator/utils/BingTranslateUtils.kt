package tw.firemaples.onscreenocr.translator.utils

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import tw.firemaples.onscreenocr.translator.BingTranslateAppTranslator
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Constants

object BingTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_BING_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp

    override fun launch(context: Context, text: String) {
        val i = Intent()
        i.action = Intent.ACTION_PROCESS_TEXT
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.component = ComponentName("com.microsoft.translator", "com.microsoft.translator.activity.translate.InAppTranslationActivity")
        i.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        i.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

        BingTranslateAppTranslator.context.startActivity(i)
    }
}
