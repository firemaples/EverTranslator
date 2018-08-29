package tw.firemaples.onscreenocr.ocr

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import kotlinx.coroutines.experimental.yield
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.threadUI
import java.io.File

internal const val EXT_TRAINED_DATA = ".traineddata"
internal const val EXT_TMP_FILE = ".tmp"
internal const val URL_TRAINE_DATA_DOWNLOAD_TEMPLATES =
        "https://github.com/firemaples/tessdata/raw/master/%s.traineddata"

internal val downloadThread = newSingleThreadContext("downloadThread")
internal val moveFileThread = newSingleThreadContext("moveFileThread")

internal val DOWNLOAD_TAG: String = "downloadTag"

object OCRDownloadTask {
    private val logger: Logger = LoggerFactory.getLogger(OCRDownloadTask::class.java)
    private val context: Context by lazy { CoreApplication.instance }

    private var latestJob: Job? = null

    private fun getOCRFile(ocrLang: String): File =
            File(OCRFileUtil.tessDataDir, ocrLang + EXT_TRAINED_DATA)

    public fun checkOCRFileExists(ocrLang: String): Boolean {
//        val tessDataDir = OCRFileUtil.tessDataDir
//        if (!tessDataDir.exists()) {
//            logger.warn("checkOcrFiles(): tess dir not found")
//            return false
//        }
//
//        val dataFile = File(tessDataDir, ocrLang + EXT_TRAINED_DATA)
        if (!getOCRFile(ocrLang).exists()) {
//            logger.warn("checkOcrFiles(): target OCR file not found")
            return false
        }

        return true
    }

    public fun cancel() {
        latestJob?.cancel()
        AndroidNetworking.cancel(DOWNLOAD_TAG)
    }

    public fun downloadOCRFiles(ocrLang: String,
                                callback: OnOCRDownloadTaskCallback) {
        latestJob = launch(downloadThread) {
            threadUI {
                callback.onDownloadStart()
            }

            //Double check file existing
            if (checkOCRFileExists(ocrLang)) {
                threadUI {
                    callback.onDownloadFinished()
                }
                return@launch
            }

            yield()
            //Make folders
            val dataDir = OCRFileUtil.tessDataDir
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                val msg = context.getString(R.string.error_makingFolderFailed, dataDir.absolutePath)
                logger.error(msg)
                threadUI {
                    callback.onError(msg)
                }
                return@launch
            }

            yield()
            //Delete temp file
            val tempFile = File(dataDir, ocrLang + EXT_TMP_FILE)
            if (tempFile.exists() && !tempFile.delete()) {
                val msg = context.getString(R.string.error_deleteTempFileFailed,
                        tempFile.absolutePath)
                logger.error(msg)
                threadUI {
                    callback.onError(msg)
                }
                return@launch
            }

            val destFile = getOCRFile(ocrLang)

            yield()
            //Download trained data //TODO using config
            val url = URL_TRAINE_DATA_DOWNLOAD_TEMPLATES.format(ocrLang)
            AndroidNetworking.download(url, tempFile.parent, tempFile.name)
                    .setPriority(Priority.HIGH)
                    .setTag(DOWNLOAD_TAG)
                    .build()
                    .setDownloadProgressListener { bytesDownloaded, totalBytes ->
                        val msg = context.getString(
                                R.string.dialog_content_progressingDownloadOCRFile,
                                ocrLang,
                                bytesDownloaded.toFloat() / 1024f / 1024f,
                                totalBytes.toFloat() / 1024f / 1024f,
                                (bytesDownloaded * 100 / totalBytes).toInt())
                        logger.debug(msg)
                        threadUI {
                            callback.downloadProgressing(bytesDownloaded, totalBytes, msg)
                        }
                    }
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            launch(moveFileThread) {
                                if (tempFile.renameTo(destFile)) {
                                    threadUI {
                                        callback.onDownloadFinished()
                                    }
                                } else {
                                    val msg = "Move file failed, from: ${tempFile.absolutePath}, " +
                                            "to: ${destFile.absolutePath}"
                                    logger.error(msg)
                                    threadUI {
                                        callback.onError(msg)
                                    }
                                }
                            }
                        }

                        override fun onError(anError: ANError?) {
                            anError?.printStackTrace()
                            threadUI {
                                callback.onError(
                                        "Download OCR file failed: ${anError?.message}")
                            }
                        }

                    })
        }
    }


}

interface OnOCRDownloadTaskCallback {
    fun onDownloadStart()

    fun onDownloadFinished()

    fun downloadProgressing(currentDownloaded: Long, totalSize: Long, msg: String)

    fun onError(errorMessage: String)
}