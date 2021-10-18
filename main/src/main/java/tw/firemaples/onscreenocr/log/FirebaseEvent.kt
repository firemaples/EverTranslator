package tw.firemaples.onscreenocr.log

import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import tw.firemaples.onscreenocr.utils.SignatureUtil
import tw.firemaples.onscreenocr.utils.Utils

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

private const val EVENT_SHOW_RESULT_WINDOW = "show_result_window"
private const val EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW = "show_google_translate_window"
private const val EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW_FAILED = "show_google_translate_window_failed"

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

//    fun logShowOCRFilesNotFoundAlert() {
//        logEvent(EVENT_SHOW_OCR_FILES_NOT_FOUND_ALERT)
//    }
//
//    fun logStartDownloadOCRFile(fileName: String, site: String) {
//        logEvent(EVENT_START_DOWNLOAD_OCR_FILE, Bundle().apply {
//            putString("file_name", fileName)
//            putString("from_site", site)
//        })
//    }
//
//    fun logOCRFileDownloadFinished() {
//        logEvent(EVENT_OCR_FILE_DOWNLOAD_FINISHED)
//    }
//
//    fun logOCRFileDownloadFailed(fileName: String, site: String, msg: String?) {
//        logEvent(EVENT_OCR_FILE_DOWNLOAD_FAILED, Bundle().apply {
//            putString("file_name", fileName)
//            putString("from_site", site)
//            putString("msg", msg)
//        })
//    }
//
//    fun logStartCaptureScreen() {
//        PerformanceTracer.startTracing(TRACE_CAPTURE_SCREENSHOT)
//        logEvent(EVENT_START_CAPTURE_SCREEN)
//    }
//
//    fun logCaptureScreenFinished() {
//        PerformanceTracer.stopTracing(TRACE_CAPTURE_SCREENSHOT, true)
//        logEvent(EVENT_CAPTURE_SCREEN_FINISHED)
//    }
//
//    fun logCaptureScreenFailed(errorCode: Int, e: Throwable?) {
//        val errorType = when (errorCode) {
//            ScreenshotHandler.ERROR_CODE_TIMEOUT -> "timeout"
//            ScreenshotHandler.ERROR_CODE_IMAGE_FORMAT_ERROR ->
//                "image_format_error"
//            ScreenshotHandler.ERROR_CODE_OUT_OF_MEMORY -> "out_of_memory"
//            ScreenshotHandler.ERROR_CODE_IO_EXCEPTION -> "io_exception"
//            ScreenshotHandler.ERROR_CODE_KNOWN_ERROR -> "unknown"
//            else -> "non_error_code"
//        }
//        val msg = e?.let {
//            logException(Exception("Capture screen failed", e))
//            it.localizedMessage
//        }
//
//        PerformanceTracer.putAttr(TRACE_CAPTURE_SCREENSHOT, "error_type", errorType)
//        msg?.also { PerformanceTracer.putAttr(TRACE_CAPTURE_SCREENSHOT, "error_msg", msg) }
//        PerformanceTracer.stopTracing(TRACE_CAPTURE_SCREENSHOT, false)
//
//        logEvent(EVENT_CAPTURE_SCREEN_FAILED, Bundle().apply {
//            putString("error_type", errorType)
//            putString("error_msg", msg)
//        })
//    }
//
//    private fun engineBundle(engine: String): Bundle = Bundle().apply {
//        putString("recognition_engine", engine)
//    }
//
//    fun logStartOCRInitializing(engine: String) {
//        PerformanceTracer.startTracing(TRACE_OCR_INITIALIZE)
//        logEvent(EVENT_START_OCR_INITIALIZING, engineBundle(engine))
//    }
//
//    fun logOCRInitialized(engine: String) {
//        PerformanceTracer.stopTracing(TRACE_OCR_INITIALIZE)
//        logEvent(EVENT_OCR_INITIALIZED, engineBundle(engine))
//    }
//
//    fun logStartOCR(engine: String) {
//        PerformanceTracer.startTracing(TRACE_OCR_PROCESS)
//        logEvent(EVENT_START_OCR, engineBundle(engine))
//    }
//
//    fun logOCRFallback(from: String, to: String) {
//        logEvent(EVENT_OCR_FALLBACK, Bundle().apply {
//            putString("from", from)
//            putString("to", to)
//        })
//    }
//
//    fun logOCRFailed(engine: String, throwable: Throwable) {
//        PerformanceTracer.stopTracing(TRACE_OCR_PROCESS)
//        logEvent(EVENT_OCR_FAILED, engineBundle(engine))
//        logException(throwable)
//    }
//
//    fun logOCRFinished(engine: String) {
//        PerformanceTracer.stopTracing(TRACE_OCR_PROCESS)
//        logEvent(EVENT_OCR_FINISHED, engineBundle(engine))
//    }
//
//    fun logStartTranslationText(
//        text: String,
//        _translateToLang: String,
//        service: TranslationService?
//    ) {
//        PerformanceTracer.startTracing(TRACE_TRANSLATE_TEXT)
//
//        val translateFromLang = OCRLangUtil.selectedLangCode
//        val textLength = text.length
//        val translateToLang = when (service) {
//            TranslationService.GoogleTranslatorApp -> "google_translation_app"
//            else -> _translateToLang
//        }
//        val serviceName = service?.name ?: "from_equals_to"
//
//        PerformanceTracer.putAttr(TRACE_TRANSLATE_TEXT, "service_name", serviceName)
//
//        val params = Bundle().apply {
//            putInt("text_length", textLength)
//            putString("translate_service", serviceName)
//            putString("translate_from", translateFromLang)
//            putString("translate_to", translateToLang)
//            putString("translate_from_to", "$translateFromLang > $translateToLang")
//            putString("device_language", Locale.getDefault().language)
//
//            if (service == TranslationService.MicrosoftAzure) {
//                putString(
//                    "microsoft_translate_group_id",
//                    RemoteConfigUtil.microsoftTranslationKeyGroupId
//                )
//            }
//        }
//
//        logEvent(EVENT_START_TRANSLATION_TEXT, params)
//    }
//
//    fun logTranslationTextFinished(service: TranslationService?) {
//        PerformanceTracer.stopTracing(TRACE_TRANSLATE_TEXT, true)
//
//        val serviceName = service?.name
//        logEvent(EVENT_TRANSLATION_TEXT_FINISHED, Bundle().apply {
//            putString("translate_service", serviceName)
//        })
//    }
//
//    fun logTranslationTextFailed(service: TranslationService?) {
//        PerformanceTracer.stopTracing(TRACE_TRANSLATE_TEXT, false)
//
//        val serviceName = service?.name
//        logEvent(EVENT_TRANSLATION_TEXT_FAILED, Bundle().apply {
//            putString("translate_service", serviceName)
//        })
//    }
//
//    fun logShowGoogleTranslateWindow() {
//        logEvent(EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW)
//    }
//
//    fun logShowGoogleTranslateWindowFailed(e: Throwable) {
//        val info = GoogleTranslateUtil.getGoogleTranslateInfo()
//        UserInfoUtils.updatePlayServiceInfo()
//
//        logException(
//            IllegalStateException(
//                "Google translate not found or version is too old: $info",
//                e
//            )
//        )
//
//        val params = Bundle().apply {
//            putString("package_info_exists", (info != null).toString())
//            putLong("version_code", info?.versionCode ?: -1)
//            putString("version_name", info?.versionName ?: "Not found")
//        }
//
//        logEvent(EVENT_SHOW_GOOGLE_TRANSLATE_WINDOW_FAILED, params)
//    }

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