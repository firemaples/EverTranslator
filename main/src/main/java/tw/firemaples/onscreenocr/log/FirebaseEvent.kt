package tw.firemaples.onscreenocr.log

import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.TimeoutCancellationException
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.translator.GoogleTranslateAppTranslator
import tw.firemaples.onscreenocr.translator.MicrosoftAzureTranslator
import tw.firemaples.onscreenocr.translator.OCROnlyTranslator
import tw.firemaples.onscreenocr.translator.Translator
import tw.firemaples.onscreenocr.utils.GoogleTranslateUtils
import tw.firemaples.onscreenocr.utils.SignatureUtil
import tw.firemaples.onscreenocr.utils.Utils
import java.io.FileNotFoundException
import java.util.*

private const val EVENT_START_AREA_SELECTION = "start_area_translation"
private const val EVENT_DRAG_SELECTION_AREA = "drag_selection_area"
private const val EVENT_RESIZE_SELECTION_AREA = "resize_selection_area"
private const val EVENT_CLICK_TRANSLATION_START_BUTTON = "click_translation_start_button"

private const val EVENT_SHOW_OCR_FILES_NOT_FOUND_ALERT = "show_ocr_files_not_found_alert"
private const val EVENT_START_DOWNLOAD_OCR_FILE = "start_download_ocr_file"
private const val EVENT_OCR_FILE_DOWNLOAD_FINISHED = "ocr_file_download_finished"
private const val EVENT_OCR_FILE_DOWNLOAD_FAILED = "ocr_file_download_failed"

private const val EVENT_START_CAPTURE_SCREEN = "start_capture_screen"
private const val EVENT_CAPTURE_SCREEN_FINISHED = "capture_screen_finished"
private const val EVENT_CAPTURE_SCREEN_FAILED = "capture_screen_failed"

private const val EVENT_START_OCR_INITIALIZING = "start_ocr_initializing"
private const val EVENT_OCR_INITIALIZED = "ocr_initialized"
private const val EVENT_START_OCR = "start_ocr"
private const val EVENT_OCR_FALLBACK = "ocr_fallback"
private const val EVENT_OCR_FAILED = "ocr_failed"
private const val EVENT_OCR_FINISHED = "ocr_finished"

private const val EVENT_START_TRANSLATION_TEXT = "start_translation_text"
private const val EVENT_TRANSLATION_TEXT_FINISHED = "translation_text_finished"
private const val EVENT_TRANSLATION_TEXT_FAILED = "translation_text_failed"
private const val EVENT_TRANSLATION_SOURCE_LANG_NOT_SUPPORT = "translation_source_lang_not_support"
private const val EVENT_MICROSOFT_TRANSLATION_OUT_OF_QUOTA =
    "event_microsoft_translation_out_of_quota"

private const val EVENT_SHOW_RESULT_WINDOW = "show_result_window"
private const val EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW = "show_google_translate_window"
private const val EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW_FAILED = "show_google_translate_window_failed"

private const val EVENT_AD_SHOW_SUCCESS = "ad_show_success"
private const val EVENT_AD_SHOW_FAILED = "ad_show_failed"

object FirebaseEvent {
    private val context by lazy { Utils.context }
    private val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }

    init {
        FirebaseApp.initializeApp(context)
    }

    fun logStartAreaSelection() {
        logEvent(EVENT_START_AREA_SELECTION)
    }

    fun logDragSelectionArea() {
        logEvent(EVENT_DRAG_SELECTION_AREA)
    }

    fun logResizeSelectionArea() {
        logEvent(EVENT_RESIZE_SELECTION_AREA)
    }

    fun logClickTranslationStartButton() {
        logEvent(EVENT_CLICK_TRANSLATION_START_BUTTON)
    }

    fun logShowOCRFilesNotFoundAlert() {
        logEvent(EVENT_SHOW_OCR_FILES_NOT_FOUND_ALERT)
    }

    fun logStartDownloadOCRFile(fileName: String, site: String) {
        logEvent(EVENT_START_DOWNLOAD_OCR_FILE, Bundle().apply {
            putString("file_name", fileName)
            putString("from_site", site)
        })
    }

    fun logOCRFileDownloadFinished() {
        logEvent(EVENT_OCR_FILE_DOWNLOAD_FINISHED)
    }

    fun logOCRFileDownloadFailed(fileName: String, site: String, msg: String?) {
        logEvent(EVENT_OCR_FILE_DOWNLOAD_FAILED, Bundle().apply {
            putString("file_name", fileName)
            putString("from_site", site)
            putString("msg", msg)
        })
    }

    fun logStartCaptureScreen() {
        PerformanceTracer.startTracing(TRACE_CAPTURE_SCREENSHOT)
        logEvent(EVENT_START_CAPTURE_SCREEN)
    }

    fun logCaptureScreenFinished() {
        PerformanceTracer.stopTracing(TRACE_CAPTURE_SCREENSHOT, true)
        logEvent(EVENT_CAPTURE_SCREEN_FINISHED)
    }

    fun logCaptureScreenFailed(e: Throwable) {
        val errorType = when {
            e is TimeoutCancellationException -> "timeout"
            e is UnsupportedOperationException -> "image_format_error"
            e is FileNotFoundException -> "io_exception"
            e.message?.contains("Buffer not large enough for pixels") == true ->
                "out_of_memory"
            else -> "unknown"
        }
        val msg = e.localizedMessage ?: e.message

        logException(Exception("Capture screen failed", e))

        PerformanceTracer.putAttr(TRACE_CAPTURE_SCREENSHOT, "error_type", errorType)
        msg?.also { PerformanceTracer.putAttr(TRACE_CAPTURE_SCREENSHOT, "error_msg", msg) }
        PerformanceTracer.stopTracing(TRACE_CAPTURE_SCREENSHOT, false)

        logEvent(EVENT_CAPTURE_SCREEN_FAILED, Bundle().apply {
            putString("error_type", errorType)
            putString("error_msg", msg)
        })
    }

    private fun engineBundle(engine: String): Bundle = Bundle().apply {
        putString("recognition_engine", engine)
    }

    fun logStartOCRInitializing(engine: String) {
        PerformanceTracer.startTracing(TRACE_OCR_INITIALIZE)
        logEvent(EVENT_START_OCR_INITIALIZING, engineBundle(engine))
    }

    fun logOCRInitialized(engine: String) {
        PerformanceTracer.stopTracing(TRACE_OCR_INITIALIZE)
        logEvent(EVENT_OCR_INITIALIZED, engineBundle(engine))
    }

    fun logStartOCR(engine: String) {
        PerformanceTracer.startTracing(TRACE_OCR_PROCESS)
        logEvent(EVENT_START_OCR, engineBundle(engine))
    }

    fun logOCRFallback(from: String, to: String) {
        logEvent(EVENT_OCR_FALLBACK, Bundle().apply {
            putString("from", from)
            putString("to", to)
        })
    }

    fun logOCRFailed(engine: String, throwable: Throwable) {
        PerformanceTracer.stopTracing(TRACE_OCR_PROCESS)
        logEvent(EVENT_OCR_FAILED, engineBundle(engine))
        logException(throwable)
    }

    fun logOCRFinished(engine: String) {
        PerformanceTracer.stopTracing(TRACE_OCR_PROCESS)
        logEvent(EVENT_OCR_FINISHED, engineBundle(engine))
    }

    fun logStartTranslationText(
        text: String,
        fromLang: String,
        translator: Translator
    ) {
        PerformanceTracer.startTracing(TRACE_TRANSLATE_TEXT)

        val textLength = text.length
        val translateToLang = when (translator) {
            is GoogleTranslateAppTranslator -> "google_translation_app"
            is OCROnlyTranslator -> "ocr_only"
            else -> AppPref.selectedTranslationLang
        }
        val serviceName = translator.type.name

        PerformanceTracer.putAttr(TRACE_TRANSLATE_TEXT, "service_name", serviceName)

        val params = Bundle().apply {
            putInt("text_length", textLength)
            putString("translate_service", serviceName)
            putString("translate_from", fromLang)
            putString("translate_to", translateToLang)
            putString("translate_from_to", "$fromLang > $translateToLang")
            putString("device_language", Locale.getDefault().language)

            if (translator is MicrosoftAzureTranslator) {
                putString(
                    "microsoft_translate_group_id",
                    RemoteConfigManager.microsoftTranslationKeyGroupId
                )
            }
        }

        logEvent(EVENT_START_TRANSLATION_TEXT, params)
    }

    fun logTranslationTextFinished(translator: Translator) {
        PerformanceTracer.stopTracing(TRACE_TRANSLATE_TEXT, true)

        val serviceName = translator.type.name
        logEvent(EVENT_TRANSLATION_TEXT_FINISHED, Bundle().apply {
            putString("translate_service", serviceName)
        })
    }

    fun logTranslationSourceLangNotSupport(translator: Translator, fromLang: String) {
        PerformanceTracer.stopTracing(TRACE_TRANSLATE_TEXT, false)

        val serviceName = translator.type.name
        logEvent(EVENT_TRANSLATION_SOURCE_LANG_NOT_SUPPORT, Bundle().apply {
            putString("translate_service", serviceName)
            putString("translate_from", fromLang)
        })
    }

    fun logTranslationTextFailed(translator: Translator) {
        PerformanceTracer.stopTracing(TRACE_TRANSLATE_TEXT, false)

        val serviceName = translator.type.name
        logEvent(EVENT_TRANSLATION_TEXT_FAILED, Bundle().apply {
            putString("translate_service", serviceName)
        })
    }

    fun logMicrosoftTranslationError(error: MicrosoftAzureTranslator.Error) {
        when (error.type) {
            MicrosoftAzureTranslator.ErrorType.OutOfQuota -> {
                logEvent(EVENT_MICROSOFT_TRANSLATION_OUT_OF_QUOTA, Bundle().apply {
                    putString("group_id", RemoteConfigManager.microsoftTranslationKeyGroupId)
                })
            }
        }
    }

    fun logShowGoogleTranslateWindow() {
        logEvent(EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW)
    }

    fun logShowGoogleTranslateWindowFailed(e: Throwable) {
        val info = GoogleTranslateUtils.getGoogleTranslateInfo()
        UserInfoUtils.updatePlayServiceInfo()

        logException(
            IllegalStateException(
                "Google translate not found or version is too old: $info",
                e
            )
        )

        val params = Bundle().apply {
            putString("package_info_exists", (info != null).toString())
            putLong("version_code", info?.versionCode ?: -1)
            putString("version_name", info?.versionName ?: "Not found")
        }

        logEvent(EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW_FAILED, params)
    }

    fun logEventAdShowSuccess(unitId: String) {
        val params = Bundle().apply {
            putString("unit_id", unitId)
        }

        logEvent(EVENT_AD_SHOW_SUCCESS, params)
    }

    fun loadEventAdShowFailed(unitId: String, errorCode: String) {
        val params = Bundle().apply {
            putString("unit_id", unitId)
            putString("error_code", errorCode)
        }

        logEvent(EVENT_AD_SHOW_FAILED, params)
    }

    private fun logEvent(key: String, params: Bundle? = null) {
        //Log event to Firebase
        firebaseAnalytics.logEvent(key, params)

        //Log event to Fabric
//        val event = CustomEvent(key).apply {
//            params.keySet().forEach { key ->
//                val value = params.get(key)
//                if (value is Number) {
//                    putCustomAttribute(key, value)
//                } else if (value is String) {
//                    putCustomAttribute(key, value)
//                }
//            }
//        }
//        Answers.getInstance().logCustom(event)
    }

    fun logException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }

    fun validateSignature() {
        val crashlytics = FirebaseCrashlytics.getInstance()
        try {
            val sha = SignatureUtil.getCurrentSignatureSHA(context)
            val result = SignatureUtil.validateSignature(context)
            crashlytics.setCustomKey("Signature_SHA", sha.toString())
            crashlytics.setCustomKey("Signature_SHA_is_correct", result)
            crashlytics.setCustomKey("Package_name", context.packageName)
        } catch (e: Throwable) {
            e.printStackTrace()
            crashlytics.setCustomKey("ValidateSignatureFailed", e.message.toString())
            crashlytics.log(Log.getStackTraceString(e))
        }
    }
}