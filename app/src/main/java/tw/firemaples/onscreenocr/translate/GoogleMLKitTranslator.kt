package tw.firemaples.onscreenocr.translate

import android.content.Context
import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Job
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView
import tw.firemaples.onscreenocr.floatingviews.screencrop.ProgressView
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.utils.*
import kotlin.jvm.Throws

object GoogleMLKitTranslator : Translator {

    private val remoteModelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

    private val translators: LruCache<TranslatorOptions, com.google.mlkit.nl.translate.Translator> =
            object : LruCache<TranslatorOptions, com.google.mlkit.nl.translate.Translator>(3) {
                override fun create(key: TranslatorOptions): com.google.mlkit.nl.translate.Translator {
                    return Translation.getClient(key)
                }

                override fun entryRemoved(evicted: Boolean, key: TranslatorOptions?, oldValue: com.google.mlkit.nl.translate.Translator?, newValue: com.google.mlkit.nl.translate.Translator?) {
                    oldValue?.close()
                }
            }

    override fun checkResource(context: Context, source: String, target: String, callback: (result: Boolean) -> Unit) {
        val sourceLang = source.toLanguage()
        val targetLang = target.toLanguage()

        if (sourceLang == null) {
            showNotSupportAlertForLang(context, source)
            callback.invoke(false)
            return
        }
        if (targetLang == null) {
            showNotSupportAlertForLang(context, target)
            callback.invoke(false)
            return
        }

        remoteModelManager.getDownloadedModels(TranslateRemoteModel::class.java).addOnSuccessListener { modelList ->
            val modelsToDownload = mutableListOf<String>()

            if (!modelList.any { it.language == sourceLang }) {
                modelsToDownload.add(sourceLang)
            }
            if (!modelList.any { it.language == targetLang }) {
                modelsToDownload.add(targetLang)
            }

            if (modelsToDownload.isNotEmpty()) {
                threadUI {
                    showAlertToDownloadModel(context, modelsToDownload.toSet().toList())

                    callback.invoke(false)
                }
            } else {
                threadUI {
                    callback.invoke(true)
                }
            }
        }.addOnFailureListener {
            threadUI {

                callback.invoke(false)
            }
        }
    }

    private fun showAlertToDownloadModel(context: Context, langToDownload: List<String>) {
        DialogView(context).apply {
            setType(DialogView.Type.CONFIRM_CANCEL)
            setTitle(R.string.btn_download.asString())
            setContentMsg(R.string.error_google_mlkit_models_are_not_downloaded.asFormatString(langToDownload.toString()))
            setCallback(object : DialogView.OnDialogViewCallback() {
                override fun onConfirmClick(dialogView: DialogView?) {
                    super.onConfirmClick(dialogView)
                    downloadResource(context, langToDownload)
                }
            })
        }.attachToWindow()
    }

    private fun downloadResource(context: Context, langToDownload: List<String>) {
        val lang = langToDownload[0]

        var taskCancelled = false

        val dialog = DialogView(context).apply {
            setType(DialogView.Type.CANCEL_ONLY)
            setTitle(R.string.btn_download.asString())
            setContentMsg(R.string.dialog_content_mlkit_model_downloading.asFormatString(lang))
            setCallback(object : DialogView.OnDialogViewCallback() {
                override fun onCancelClicked(dialogView: DialogView?) {
                    super.onCancelClicked(dialogView)
                    taskCancelled = true
                }
            })
            attachToWindow()
        }

        remoteModelManager.download(TranslateRemoteModel.Builder(lang).build(), DownloadConditions.Builder().build())
                .addOnSuccessListener {
                    dialog.detachFromWindow()

                    if (taskCancelled) return@addOnSuccessListener

                    val list = langToDownload.toMutableList()
                    list.remove(lang)

                    if (list.isEmpty()) {
                        DialogView(context).apply {
                            setType(DialogView.Type.CONFIRM_ONLY)
                            setTitle(R.string.btn_download.asString())
                            setContentMsg(R.string.dialog_content_mlkit_models_downloaded.asString())
                        }.attachToWindow()
                    } else {
                        downloadResource(context, list)
                    }
                }.addOnFailureListener {
                    dialog.detachFromWindow()

                    DialogView(context).apply {
                        setType(DialogView.Type.CONFIRM_ONLY)
                        setTitle(R.string.dialog_title_error.asString())
                        setContentMsg(R.string.error_download_mlkit_model_failed.asFormatString(it.message))
                    }.attachToWindow()
                }
    }

    private fun showNotSupportAlertForLang(context: Context, lang: String) {
        DialogView(context).apply {
            setType(DialogView.Type.CONFIRM_ONLY)
            setTitle(R.string.dialog_title_error.asString())
            setContentMsg(R.string.error_google_mlkit_is_not_support_the_source_language.asFormatString(lang))
        }.attachToWindow()
    }

    override fun translate(text: String, lang: String, callback: (Boolean, String, Throwable?) -> Unit): Job = threadTranslation.launch {
        val options = getOptions(OCRLangUtil.selectLangDisplayCode, lang).build()
        translators[options].translate(text).addOnSuccessListener {
            threadUI {
                callback.invoke(true, it, null)
            }
        }.addOnFailureListener {
            threadUI {
                callback.invoke(false, "", it)
            }
        }
    }

    @Throws(IllegalArgumentException::class)
    private fun getOptions(source: String, target: String): TranslatorOptions.Builder {
        val sourceLang = TranslateLanguage.fromLanguageTag(source.split("-")[0])
        val targetLang = TranslateLanguage.fromLanguageTag(target)

        if (sourceLang == null) throw IllegalArgumentException("The source language is not supported")
        if (targetLang == null) throw IllegalArgumentException("The target lang is not supported")

        return TranslatorOptions.Builder().setSourceLanguage(sourceLang).setTargetLanguage(targetLang)
    }

    private fun String.toLanguage(): String? =
            TranslateLanguage.fromLanguageTag(this.split("-")[0])
}
