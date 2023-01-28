package tw.firemaples.onscreenocr.translator

import kotlinx.coroutines.CoroutineScope
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.translator.utils.TranslatorUtils
import tw.firemaples.onscreenocr.utils.Logger

abstract class BaseAppTranslator : Translator {
    protected val logger: Logger by lazy { Logger(this::class) }

    abstract val translatorUtils: TranslatorUtils

    override val translationHint: String
        get() = context.getString(
            R.string.msg_select_lang_in_translate_app,
            context.getString(type.nameRes)
        )

    override suspend fun checkEnvironment(
        coroutineScope: CoroutineScope
    ): Boolean = translatorUtils.checkIsInstalled()

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
        translatorUtils.launchTranslator(text)
        return TranslationResult.OuterTranslatorLaunched
    }
}