package tw.firemaples.onscreenocr.ocr.tesseract

import android.content.Context
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatings.screencrop.DialogView
import tw.firemaples.onscreenocr.floatings.screencrop.SingleSelectDialogView
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.log.UserInfoUtils
import tw.firemaples.onscreenocr.ocr.OCRDownloadTask
import tw.firemaples.onscreenocr.ocr.OnOCRDownloadTaskCallback
import tw.firemaples.onscreenocr.ocr.event.OCRLangChangedEvent
import tw.firemaples.onscreenocr.utils.BaseSettingUtil
import java.util.*

object OCRLangUtil : BaseSettingUtil() {
    private const val DEFAULT_LANG = "eng"
    private const val KEY_RECOGNITION_SERVICE = "preference_recognition_service"
    private const val KEY_RECOGNITION_LANGUAGE = "preference_list_recognition_language"
    private const val KEY_PAGE_SEGMENTATION_MODE = "preference_list_page_segmentation_mode"

    val pageSegmentationModeList: Array<String> by lazy {
        context.resources.getStringArray(R.array.pagesegmentationmodes)
    }

    val pageSegmentationMode: String
        get() = sp.getString(KEY_PAGE_SEGMENTATION_MODE, pageSegmentationModeList[0])
                ?: pageSegmentationModeList[0]

    val pageSegmentationModeIndex: Int
        get() = pageSegmentationModeList.indexOf(pageSegmentationMode)

    val ocrLangCodeList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langCode_iso6393)
    }

    val ocrLangNameList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langName)
    }

    val ocrLangDisplayCodeList: Array<String> by lazy {
        context.resources.getStringArray(R.array.ocr_langDisplayCode)
    }

    var selectedOCRService: OCRService
        get() = OCRService.fromId(sp.getInt(KEY_RECOGNITION_SERVICE, -1))
                ?: OCRService.GoogleMLKit
        set(value) {
            sp.edit().putInt(KEY_RECOGNITION_SERVICE, value.id).apply()
//            val displayCode =
//            EventUtil.post(OCRLangChangedEvent(value, displayCode))
//            UserInfoUtils.updateClientSettings()
        }

    var selectedLangCode: String
        get() = sp.getString(KEY_RECOGNITION_LANGUAGE, DEFAULT_LANG) ?: DEFAULT_LANG
        set(value) {
            if (selectedLangCode == value) return
            sp.edit().putString(KEY_RECOGNITION_LANGUAGE, value).apply()
            val displayCode = ocrLangDisplayCodeList[ocrLangCodeList.indexOf(value)]
            EventUtil.post(OCRLangChangedEvent(value, displayCode))
            UserInfoUtils.updateClientSettings()
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
        FirebaseEvent.logShowOCRFilesNotFoundAlert()

        val dialogView = DialogView(context)
        dialogView.reset()
        dialogView.setTitle(context.getString(R.string.dialog_title_ocrFileNotFound))
        dialogView.setContentMsg(String.format(Locale.getDefault(),
                context.getString(R.string.dialog_content_ocrFileNotFound),
                selectedLangName))
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
            OCRDownloadTask.downloadOCRFiles(selectedLangCode, callback)
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

    enum class OCRService(val id: Int) {
        GoogleMLKit(id = 0),
        Tesseract(id = 1);

        companion object {
            fun fromId(id: Int): OCRService? = OCRService.values().firstOrNull { it.id == id }
        }
    }
}