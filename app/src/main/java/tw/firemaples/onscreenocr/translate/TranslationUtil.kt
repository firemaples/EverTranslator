package tw.firemaples.onscreenocr.translate

import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.translate.event.TranslationLangChangedEvent
import tw.firemaples.onscreenocr.translate.event.TranslationServiceChangedEvent
import tw.firemaples.onscreenocr.utils.BaseSettingUtil
import tw.firemaples.onscreenocr.utils.Utils.Companion.context

object TranslationUtil : BaseSettingUtil() {
    private const val DEFAULT_SERVICE = 2
    private const val KEY_TRANSLATE_SERVICE = "translateService"
    private const val KEY_CURRENT_TRANSLATE_LANG_CODE = "currentTranslateLangCode"

    var currentService: TranslationService
        get() = TranslationService.values()
                .first { it.id == sp.getInt(KEY_TRANSLATE_SERVICE, DEFAULT_SERVICE) }
        set(value) {
            if (currentService == value) return
            sp.edit().putInt(KEY_TRANSLATE_SERVICE, value.id).apply()
            currentTranslationLangCode = value.defaultLangCode
            EventUtil.post(TranslationServiceChangedEvent(value))
        }

    val serviceList = TranslationService.values().sortedBy { it.sort }

    val currentServiceIndex: Int
        get() = serviceList.indexOf(currentService)

    val translationLangCodeList: Array<String>
        get() = when (currentService) {
            TranslationService.MicrosoftAzure ->
                context.resources.getStringArray(R.array.microsoft_translationLangCode_iso6391)
            TranslationService.Yandex ->
                context.resources.getStringArray(R.array.yandex_translationLangCode)
            TranslationService.GoogleTranslatorApp, TranslationService.DisableTranslation ->
                arrayOf()
        }

    fun getTranslationLangNameList(service: TranslationService = currentService): Array<String> =
            when (service) {
                TranslationService.MicrosoftAzure ->
                    context.resources.getStringArray(
                            R.array.microsoft_translationLangName)
                TranslationService.Yandex ->
                    context.resources.getStringArray(
                            R.array.yandex_translationLangName)
                TranslationService.GoogleTranslatorApp, TranslationService.DisableTranslation ->
                    arrayOf()
            }

    var currentTranslationLangIndex
        get() = translationLangCodeList.indexOf(currentTranslationLangCode)
        set(value) {
            currentTranslationLangCode = translationLangCodeList[value]
        }

    var currentTranslationLangCode: String
        get() = sp.getString(KEY_CURRENT_TRANSLATE_LANG_CODE, null)
                ?: currentService.defaultLangCode
        set(value) {
            if (currentTranslationLangCode == value) return
            sp.edit().putString(KEY_CURRENT_TRANSLATE_LANG_CODE, value).apply()
            EventUtil.post(TranslationLangChangedEvent(value))
        }

    private fun getTranslationLangCode(index: Int) =
            if (translationLangCodeList.isNotEmpty())
                translationLangCodeList[index]
            else
                currentService.fullName

    val isEnableTranslation: Boolean
        get() = currentService != TranslationService.DisableTranslation
}

enum class TranslationService(val id: Int, val sort: Int,
                              val fullName: String,
                              val defaultLangCode: String) {
    MicrosoftAzure(0, 0, context.getString(R.string.service_microsoft_azure), defaultLangCode = "en"),
    Yandex(1, 1, context.getString(R.string.service_yandex), defaultLangCode = "en"),
    GoogleTranslatorApp(2, 2, context.getString(R.string.service_google_translate_app), defaultLangCode = "Google"),
    DisableTranslation(999, 999, context.getString(R.string.service_disable_translation), defaultLangCode = "None");

    val isCurrent: Boolean
        get() = TranslationUtil.currentService == this
}