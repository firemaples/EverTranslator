package tw.firemaples.onscreenocr.log

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import tw.firemaples.onscreenocr.utils.Logger

const val TRACE_CAPTURE_SCREENSHOT = "capture_screenshot"

const val TRACE_OCR_INITIALIZE = "ocr_initialize"

const val TRACE_OCR_PROCESS = "ocr_process"

const val TRACE_TRANSLATE_TEXT = "translate_text"

const val ATTR_SUCCESS = "success"
const val ATTR_MISSING_STOP = "missing_stop"

object PerformanceTracer {
    private val logger by lazy { Logger(PerformanceTracer::class) }
    private val firebasePerformance by lazy { FirebasePerformance.getInstance() }

    private val traceMap = mutableMapOf<String, Trace>()

    fun startTracing(key: String) = synchronized(this) {
        if (isTracing(key)) {
            logger.warn("Tracing key [$key] exist when starting a new tracing, force stop previous")
            putAttr(key, ATTR_MISSING_STOP, "true")
            stopTracing(key)
        }

        logger.info("Start tracing for key [$key]")
        val trace = firebasePerformance.newTrace(key)
        trace.start()
        traceMap[key] = trace
    }

    fun putAttr(key: String, attr: String, value: String) = synchronized(this) {
        getTrace(key)?.also {
            logger.debug("Put attr [$attr] for tracing [$key]")
            it.putAttribute(attr, value)
        }
    }

    fun putMetric(key: String, attr: String, value: Long) = synchronized(this) {
        getTrace(key)?.also {
            logger.debug("Put metric [$attr] for tracing [$key]")
            it.putMetric(attr, value)
        }
    }

    fun increaseMetric(key: String, attr: String, value: Long) = synchronized(this) {
        getTrace(key)?.also {
            logger.debug("Increase metric [$attr] for tracing [$key] with $value")

            val oldValue = it.getLongMetric(attr)
            it.incrementMetric(attr, value)
            logger.debug("Metric changed for [$key], $oldValue -> ${it.getLongMetric(attr)}")
        }
    }

    fun stopTracing(key: String, isSuccess: Boolean? = null) = synchronized(this) {
        getTrace(key)?.also {
            isSuccess?.also { success ->
                logger.debug("Set tracing ${if (success) "SUCCESS" else "FAILED"} for [$key]")
                putAttr(key, ATTR_SUCCESS, success.toString())
            }

            logger.info("Stop tracing for key [$key]")
            it.stop()
            traceMap.remove(key)
        }
    }

    private fun isTracing(key: String): Boolean = traceMap.containsKey(key)

    private fun getTrace(key: String): Trace? = traceMap[key]
}