package tw.firemaples.onscreenocr.translator

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import tw.firemaples.onscreenocr.R
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object GoogleMLKitTranslator : Translator {
    private val remoteModelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    override val type: TranslationProviderType
        get() = TranslationProviderType.GoogleMLKit

    private var lastTranslatorLangKey: String? = null
    private var lastTranslator: com.google.mlkit.nl.translate.Translator? = null

    override suspend fun supportedLanguages(): List<TranslationLanguage> {
        val langCodeList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangCode_iso6391)
        val langNameList =
            context.resources.getStringArray(R.array.google_MLKit_translationLangName)

        val selectedLangCode = selectedLangCode(langCodeList)

        return (langCodeList.indices).map { i ->
            val code = langCodeList[i]
            val name = langNameList[i]

            TranslationLanguage(
                code = code,
                displayName = name,
                selected = code == selectedLangCode
            )
        }
    }

    override suspend fun checkResources(langList: List<String>): List<String> =
        suspendCoroutine {
            remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { modelList ->
                    it.resume(langList - modelList.map { it.language })
                }
                .addOnFailureListener { e ->
                    it.resumeWithException(e)
                }
        }

    override suspend fun downloadResources(langList: List<String>) {
        for (lang in langList) {
            suspendCoroutine<Any> { c ->
                remoteModelManager.download(
                    TranslateRemoteModel.Builder(lang).build(),
                    DownloadConditions.Builder().build()
                ).addOnSuccessListener {
                    c.resume(Any())
                }.addOnFailureListener {
                    c.resumeWithException(it)
                }
            }
        }
    }

    override suspend fun translate(text: String, sourceLangCode: String): TranslationResult {
        val targetLangCode = supportedLanguages().firstOrNull { it.selected }?.code
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Selected language code is not found"))
        val sourceLang = TranslateLanguage.fromLanguageTag(sourceLangCode)
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Parsing language tag failed, sourceLangCode: $sourceLangCode"))
        val targetLang = TranslateLanguage.fromLanguageTag(targetLangCode)
            ?: return TranslationResult.TranslationFailed(IllegalArgumentException("Parsing language tag failed, targetLangCode: $targetLangCode"))

        val langKey = "${sourceLang}_$targetLang"

        val lastTranslatorLangKey = lastTranslatorLangKey
        val lastTranslator = lastTranslator

        val client =
            if (lastTranslatorLangKey == langKey && lastTranslator != null) lastTranslator
            else {
                if (lastTranslator != null) {
                    lastTranslator.close()
                    this.lastTranslator = null
                }

                Translation.getClient(
                    TranslatorOptions.Builder()
                        .setSourceLanguage(sourceLang)
                        .setTargetLanguage(targetLang)
                        .build()
                ).also {
                    this.lastTranslatorLangKey = langKey
                    this.lastTranslator = it
                }
            }

        return suspendCoroutine { c ->
            client.translate(text)
                .addOnSuccessListener {
                    c.resume(
                        TranslationResult.TranslatedResult(
                            it, TranslationProviderType.GoogleMLKit
                        )
                    )
                }
                .addOnFailureListener {
                    c.resumeWithException(it)
                }
        }
    }
}
