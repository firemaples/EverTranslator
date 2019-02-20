package tw.firemaples.onscreenocr.ocr

import android.content.Context
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.DownloadListener
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.utils.threadUI
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

internal const val EXT_TRAINED_DATA = ".traineddata"
internal const val EXT_TMP_FILE = ".tmp"

internal val downloadThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
internal val moveFileThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

internal val DOWNLOAD_TAG: String = "downloadTag"

object OCRDownloadTask {
    private val logger: Logger = LoggerFactory.getLogger(OCRDownloadTask::class.java)
    private val context: Context by lazy { CoreApplication.instance }

    private var latestJob: Job? = null

    private fun getBaseFolder(): File = OCRFileUtil.tessDataDir

    private fun notExistOCRFiles(ocrLang: String): List<String> =
            RemoteConfigUtil.trainedDataFileSubs(ocrLang).map {
                ocrLang + it
            }.filter { !File(getBaseFolder().absolutePath, it).exists() }

    fun checkOCRFileExists(ocrLang: String): Boolean = notExistOCRFiles(ocrLang).isEmpty()

    public fun cancel() {
        latestJob?.cancel()
        AndroidNetworking.cancel(DOWNLOAD_TAG)
    }

    fun genTempFilePath(ocrFilePath: String): File = File(getBaseFolder(),
            ocrFilePath + EXT_TMP_FILE)

    public fun downloadOCRFiles(ocrLang: String,
                                callback: OnOCRDownloadTaskCallback) {
        latestJob = GlobalScope.launch(downloadThread) {
            threadUI {
                callback.onDownloadStart()
            }

            yield()
            //Make folders
            val dataDir = getBaseFolder()
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                val msg = context.getString(R.string.error_makingFolderFailed, dataDir.absolutePath)
                logger.error(msg)
                threadUI {
                    callback.onError(msg)
                }
                return@launch
            }

            yield()
            val ocrFilePaths = notExistOCRFiles(ocrLang)
            if (ocrFilePaths.isEmpty()) {
                threadUI {
                    callback.onDownloadFinished()
                }
                return@launch
            }

            val fileName = ocrFilePaths[0]

            val site = OCRFileUtil.trainedDataDownloadSiteKey
            val url = OCRFileUtil.trainedDataDownloadSite.url.format(fileName)
            val destFile = File(getBaseFolder().absolutePath, fileName)

            //Delete temp file
            val tempFile = genTempFilePath(fileName)
            if (tempFile.exists() && !tempFile.delete()) {
                val msg = context.getString(R.string.error_deleteTempFileFailed,
                        tempFile.absolutePath)
                logger.error(msg)
                threadUI {
                    callback.onError(msg)
                }
                return@launch
            }

            //Download trained data
            logger.info("Start download ocr file from: $url")
            FirebaseEvent.logStartDownloadOCRFile(fileName, site)
            AndroidNetworking.download(url, tempFile.parent, tempFile.name)
                    .setPriority(Priority.HIGH)
                    .setTag(DOWNLOAD_TAG)
                    .build()
                    .setDownloadProgressListener { bytesDownloaded, _totalBytes ->
                        val totalBytes = Math.max(_totalBytes, bytesDownloaded)
                        val msg = context.getString(
                                R.string.dialog_content_progressingDownloadOCRFile,
                                OCRLangUtil.getLangName(ocrLang),
                                fileName,
                                bytesDownloaded.toFloat() / 1024f / 1024f,
                                totalBytes.toFloat() / 1024f / 1024f,
                                (bytesDownloaded * 100 / totalBytes).toInt())
//                        logger.debug(msg)
                        threadUI {
                            callback.downloadProgressing(bytesDownloaded, totalBytes, msg)
                        }
                    }
                    .startDownload(object : DownloadListener {
                        override fun onDownloadComplete() {
                            GlobalScope.launch(moveFileThread) {
                                if (tempFile.renameTo(destFile)) {
                                    FirebaseEvent.logOCRFileDownloadFinished()
                                    threadUI {
                                        downloadOCRFiles(ocrLang, callback)
                                    }
                                } else {
                                    val msg = "Move file failed, from: ${tempFile.absolutePath}, " +
                                            "to: ${destFile.absolutePath}"
                                    logger.error(msg)
                                    FirebaseEvent.logException(IOException(msg))
                                    FirebaseEvent.logOCRFileDownloadFailed(fileName, site, msg)
                                    threadUI {
                                        callback.onError(msg)
                                    }
                                }
                            }
                        }

                        override fun onError(anError: ANError?) {
                            if (anError != null) {
                                anError.printStackTrace()
                                FirebaseEvent.logException(anError)
                                FirebaseEvent.logOCRFileDownloadFailed(fileName, site,
                                        anError.localizedMessage)
                            } else {
                                val msg = "An unknown download failed found"
                                Exception(msg).also {
                                    it.printStackTrace()
                                    FirebaseEvent.logException(it)
                                }
                                FirebaseEvent.logOCRFileDownloadFailed(fileName, site, msg)
                            }
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