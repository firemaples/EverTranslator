package tw.firemaples.onscreenocr.translate

import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.log.UserInfoUtils
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

    private val translationLangCodeListMicrosoft: Array<String> by lazy {
        context.resources.getStringArray(R.array.microsoft_translationLangCode_iso6391)
    }
    private val translationLangCodeListYandex: Array<String> by lazy {
        context.resources.getStringArray(R.array.yandex_translationLangCode)
    }
    private val translationLangCodeListGoogle: Array<String> by lazy {
        context.resources.getStringArray(R.array.google_translationLangCode_iso6391)
    }
    val translationLangCodeList: Array<String>
        get() = when (currentService) {
            TranslationService.MicrosoftAzure -> translationLangCodeListMicrosoft
            TranslationService.Yandex -> translationLangCodeListYandex
            TranslationService.GoogleTranslator -> translationLangCodeListGoogle
            TranslationService.GoogleTranslatorApp, TranslationService.OCROnly ->
                arrayOf()
        }

    private val getTranslationLangNameListMicrosoft: Array<String> by lazy {
        context.resources.getStringArray(
                R.array.microsoft_translationLangName)
    }
    private val getTranslationLangNameListYandex: Array<String> by lazy {
        context.resources.getStringArray(
                R.array.yandex_translationLangName)
    }
    private val getTranslationLangNameListGoogle: Array<String> by lazy {
        context.resources.getStringArray(
                R.array.google_translationLangName)
    }

    fun getTranslationLangNameList(service: TranslationService = currentService): Array<String> =
            when (service) {
                TranslationService.MicrosoftAzure -> getTranslationLangNameListMicrosoft
                TranslationService.Yandex -> getTranslationLangNameListYandex
                TranslationService.GoogleTranslator -> getTranslationLangNameListGoogle
                TranslationService.GoogleTranslatorApp, TranslationService.OCROnly ->
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
            UserInfoUtils.updateClientSettings()
        }

    private fun getTranslationLangCode(index: Int) =
            if (translationLangCodeList.isNotEmpty())
                translationLangCodeList[index]
            else
                currentService.fullName

    val isEnableTranslation: Boolean
        get() = currentService != TranslationService.OCROnly
}

enum class TranslationService(val id: Int, val sort: Int,
                              val fullName: String,
                              val defaultLangCode: String) {
    GoogleTranslator(3, 0, context.getString(R.string.service_google_translate), defaultLangCode = "en"),
    MicrosoftAzure(0, 1, context.getString(R.string.service_microsoft_azure), defaultLangCode = "en"),
    Yandex(1, 2, context.getString(R.string.service_yandex), defaultLangCode = "en"),
    GoogleTranslatorApp(2, 3, context.getString(R.string.service_google_translate_app), defaultLangCode = "Google"),
    OCROnly(999, 999, context.getString(R.string.service_ocr_only), defaultLangCode = "None");

    val isCurrent: Boolean
        get() = TranslationUtil.currentService == this
}