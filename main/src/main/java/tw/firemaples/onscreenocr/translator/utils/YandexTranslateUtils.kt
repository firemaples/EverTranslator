package tw.firemaples.onscreenocr.translator.utils

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.translator.YandexTranslateAppTranslator
import tw.firemaples.onscreenocr.utils.Constants

object YandexTranslateUtils : TranslatorUtils() {
    override val packageName: String
        get() = Constants.PACKAGE_NAME_YANDEX_TRANSLATE

    override val type: TranslationProviderType
        get() = TranslationProviderType.YandexTranslateApp

    override fun launch(context: Context, text: String) {
        val i = Intent()
        i.action = Intent.ACTION_PROCESS_TEXT
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.component = ComponentName("ru.yandex.translate", "ru.yandex.translate.ui.activities.QuickTrActivity")
        i.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        i.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

        YandexTranslateAppTranslator.context.startActivity(i)
    }
}
