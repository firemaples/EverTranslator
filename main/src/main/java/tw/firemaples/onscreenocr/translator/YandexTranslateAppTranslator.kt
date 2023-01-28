package tw.firemaples.onscreenocr.translator

import android.content.ClipDescription
import android.content.ComponentName
import android.content.Intent
import tw.firemaples.onscreenocr.R


object YandexTranslateAppTranslator: Translator {
    override val type: TranslationProviderType
        get() = TranslationProviderType.BingTranslateApp

    override val translationHint: String
        get() = context.getString(R.string.msg_select_lang_in_yandex_translate)

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {

        val i = Intent()
        i.action = Intent.ACTION_PROCESS_TEXT
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.type = ClipDescription.MIMETYPE_TEXT_PLAIN
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.component = ComponentName("ru.yandex.translate", "ru.yandex.translate.ui.activities.QuickTrActivity")
        i.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
        i.putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)

        context.startActivity(i)


        return TranslationResult.OuterTranslatorLaunched
    }

}