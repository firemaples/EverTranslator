package tw.firemaples.onscreenocr.translator

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pref.AppPref
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

    override suspend fun checkEnvironment(coroutineScope: CoroutineScope): Boolean =
        checkTranslationResources(coroutineScope)

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
                            it, type
                        )
                    )
                }
                .addOnFailureListener {
                    c.resumeWithException(it)
                }
        }
    }

    private suspend fun checkTranslationResources(coroutineScope: CoroutineScope): Boolean {
        val langList = listOf(AppPref.selectedOCRLang, AppPref.selectedTranslationLang)

        val langToDownload = try {
            checkResources(langList)
        } catch (e: Exception) {
            FirebaseEvent.logException(e)

            DialogView(context).apply {
                setTitle(context.getString(R.string.title_failed_to_check_resources))
                setMessage(e.localizedMessage ?: context.getString(R.string.error_unknown))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()
            return false
        }

        if (langToDownload.isNotEmpty()) {
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_download))
                setMessage(
                    context.getString(R.string.msg_models_to_download) +
                            "\n\n${langToDownload.joinToString(", ")}"
                )
                setDialogType(DialogView.DialogType.CONFIRM_CANCEL)

                onButtonOkClicked = {
                    coroutineScope.launch {
                        downloadTranslationResources(langToDownload)
                    }
                }
            }.attachToScreen()

            return false
        }
        return true
    }

    private suspend fun checkResources(langList: List<String>): List<String> =
        suspendCoroutine {
            remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { modelList ->
                    it.resume(langList - modelList.map { it.language })
                }
                .addOnFailureListener { e ->
                    it.resumeWithException(e)
                }
        }

    private suspend fun downloadResources(langList: List<String>) {
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

    private suspend fun downloadTranslationResources(langList: List<String>) {
        val dialog = DialogView(context).apply {
            setTitle(context.getString(R.string.title_resources_downloading))
            setMessage(context.getString(R.string.msg_wait_for_resources_downloading))
            setDialogType(DialogView.DialogType.CANCEL_ONLY)

            attachToScreen()
        }

        try {
            downloadResources(langList)

            dialog.detachFromScreen()
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_resouces_downloaded))
                setMessage(context.getString(R.string.msg_resouces_downloaded))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()
        } catch (e: Exception) {
            FirebaseEvent.logException(e)

            dialog.detachFromScreen()
            DialogView(context).apply {
                setTitle(context.getString(R.string.title_downloading_resouces_failed))
                setMessage(e.localizedMessage ?: context.getString(R.string.error_unknown))
                setDialogType(DialogView.DialogType.CONFIRM_ONLY)
            }.attachToScreen()
        }
    }
}
