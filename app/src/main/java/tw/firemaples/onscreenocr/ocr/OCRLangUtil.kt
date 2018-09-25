package tw.firemaples.onscreenocr.ocr

import android.content.Context
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.screencrop.DialogView
import tw.firemaples.onscreenocr.floatingviews.screencrop.SingleSelectDialogView
import tw.firemaples.onscreenocr.ocr.event.OCRLangChangedEvent
import tw.firemaples.onscreenocr.utils.BaseSettingUtil
import java.util.*

object OCRLangUtil : BaseSettingUtil() {
    private const val DEFAULT_LANG = "eng"
    private const val KEY_RECOGNITION_LANGUAGE = "preference_list_recognition_language"

    val ocrLangCodeList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langCode_iso6393)
    }

    val ocrLangNameList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langName)
    }

    val ocrLangDisplayCodeList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langDisplayCode)
    }

    var selectedLangCode: String
        get() = sp.getString(KEY_RECOGNITION_LANGUAGE, DEFAULT_LANG) ?: DEFAULT_LANG
        set(value) {
            if (selectedLangCode == value) return
            sp.edit().putString(KEY_RECOGNITION_LANGUAGE, value).apply()
            val displayCode = ocrLangDisplayCodeList[ocrLangCodeList.indexOf(value)]
            EventUtil.post(OCRLangChangedEvent(value, displayCode))
        }

    val selectedLangName: String
        get() = ocrLangNameList[selectedLangIndex]

    var selectedLangIndex: Int
        get() = ocrLangCodeList.indexOf(selectedLangCode)
        set(value) {
            if (selectedLangIndex == value) return
            selectedLangCode = ocrLangCodeList[value]
        }

    val selectLangDisplayCode: String
        get() = ocrLangDisplayCodeList[selectedLangIndex]

    fun getLangName(ocrLang: String): String = ocrLangNameList[ocrLangCodeList.indexOf(ocrLang)]

    fun checkCurrentOCRFiles(): Boolean = OCRDownloadTask.checkOCRFileExists(selectedLangCode)

    fun showDownloadAlertDialog() {
        val dialogView = DialogView(context)
        dialogView.reset()
        dialogView.setTitle(context.getString(R.string.dialog_title_ocrFileNotFound))
        dialogView.setContentMsg(String.format(Locale.getDefault(),
                context.getString(R.string.dialog_content_ocrFileNotFound),
                OCRLangUtil.selectedLangName))
        dialogView.setType(DialogView.Type.CONFIRM_CANCEL)
        dialogView.okBtn.setText(R.string.btn_download)
        dialogView.setCallback(object : DialogView.OnDialogViewCallback() {
            override fun onConfirmClick(dialogView: DialogView) {
                super.onConfirmClick(dialogView)
                OCRDownloadingDialogView(context).attachToWindow()
            }
        })
        dialogView.attachToWindow()
    }

    private class OCRDownloadingDialogView(context: Context) : DialogView(context) {

        val callback: OnOCRDownloadTaskCallback = object :
                OnOCRDownloadTaskCallback {
            override fun onDownloadStart() {
                reset()
                setType(DialogView.Type.CANCEL_ONLY)
                setTitle(getContext().getString(R.string.dialog_title_ocrFileDownloading))
                setContentMsg(getContext()
                        .getString(R.string.dialog_content_ocrFileDownloadStarting))
                setCallback(object : DialogView.OnDialogViewCallback() {
                    override fun onCancelClicked(dialogView: DialogView) {
                        OCRDownloadTask.cancel()
                        super.onCancelClicked(dialogView)
                    }
                })
            }

            override fun onDownloadFinished() {
                detachFromWindow()
            }

            override fun downloadProgressing(currentDownloaded: Long, totalSize: Long,
                                             msg: String) {
                setContentMsg(msg)
            }

            override fun onError(errorMessage: String) {
                rootView.post {
                    reset()
                    setType(Type.CONFIRM_CANCEL)
                    setTitle(getContext().getString(R.string.dialog_title_error))
                    val text = context.getString(
                            R.string.error_ocrFilesDownloadFailed, errorMessage)
                    setContentMsg(text)
                    okBtn.text = context.getString(R.string.change)
                    setCallback(object : OnDialogViewCallback() {
                        override fun onConfirmClick(dialogView: DialogView?) {
                            super.onConfirmClick(dialogView)

                            showOCRFileSourceSiteSelector(context) {
                                OCRDownloadingDialogView(context).attachToWindow()
                            }
                        }
                    })
                }
            }
        }

        override fun attachToWindow() {
            super.attachToWindow()
            OCRDownloadTask.downloadOCRFiles(OCRLangUtil.selectedLangCode, callback)
        }

        override fun detachFromWindow() {
            super.detachFromWindow()
            OCRDownloadTask.cancel()
        }
    }

    private fun showOCRFileSourceSiteSelector(context: Context, callback: () -> Unit) {
        SingleSelectDialogView(context, context.getString(R.string.select_download_site),
                OCRFileUtil.trainedDataSites.map { it.name }.toTypedArray()) {
            OCRFileUtil.trainedDataDownloadSiteIndex = it
            callback()
        }.attachToWindow()
    }
}