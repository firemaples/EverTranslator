package tw.firemaples.onscreenocr.ocr

import android.graphics.Bitmap
import android.graphics.Rect
import android.os.AsyncTask
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.utils.ImageFile
import tw.firemaples.onscreenocr.utils.Utils.Companion.context
import java.io.File

object OCRManager {
    val tessBaseAPI: TessBaseAPI by lazy { TessBaseAPI() }

    private var callback: OnOCRStateChangedListener? = null
    private var lastAsyncTask: AsyncTask<*, *, *>? = null

    private var currentScreenshot: ImageFile? = null
    private var boxList: List<Rect>? = null

    fun setListener(callback: OnOCRStateChangedListener) {
        this.callback = callback
    }

    fun start(screenshot: ImageFile, boxList: List<Rect>) {
        this.currentScreenshot = screenshot
        this.boxList = boxList

        initOcrEngine()
    }

    fun cancelRunningTask() {
        lastAsyncTask?.cancel(true)
    }

    private fun initOcrEngine() {
        callback?.onInitializing()

        lastAsyncTask = OcrInitAsyncTask(context, onOcrInitAsyncTaskCallback).execute()
    }

    private val onOcrInitAsyncTaskCallback = object :
            OcrInitAsyncTask.OnOcrInitAsyncTaskCallback {
        override fun onOcrInitialized() {
            callback?.onInitialized()
            startTextRecognize()
        }

        override fun showMessage(message: String) {}

        override fun hideMessage() {}
    }

    private fun startTextRecognize() {
        callback?.onRecognizing()

        lastAsyncTask = OcrRecognizeAsyncTask(context,
                currentScreenshot,
                boxList,
                onTextRecognizeAsyncTaskCallback).execute()
    }

    private val onTextRecognizeAsyncTaskCallback = object :
            OcrRecognizeAsyncTask.OnTextRecognizeAsyncTaskCallback {
        override fun onTextRecognizeFinished(results: List<OcrResult>) {
            callback?.onRecognized(results)
        }

        override fun showMessage(message: String) {}

        override fun hideMessage() {}
    }


    interface OnOCRStateChangedListener {
        fun onInitializing()

        fun onInitialized()

        fun onRecognizing()

        fun onRecognized(results: List<OcrResult>)
    }
}