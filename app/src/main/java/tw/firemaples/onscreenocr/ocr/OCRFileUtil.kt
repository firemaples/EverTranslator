package tw.firemaples.onscreenocr.ocr

import android.annotation.SuppressLint
import android.os.Build
import android.os.Environment
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.log.UserInfoUtils
import tw.firemaples.onscreenocr.ocr.OCRFileUtil.removableFileDirOrNormalFilesDir
import tw.firemaples.onscreenocr.ocr.event.TrainedDataDownloadSiteChangedEvent
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.remoteconfig.data.TrainedDataSite
import tw.firemaples.onscreenocr.utils.BaseSettingUtil
import java.io.File

private const val KEY_TRAINED_DATA_SITE_KEY = "KEY_TRAINED_DATA_SITE_KEY"
private const val DEFAULT_TRAINED_DATA_SITE_KEY = "github"

private const val KEY_TESS_DATA_LOCATION = "preference_tess_data_location"

object OCRFileUtil : BaseSettingUtil() {
    var trainedDataDownloadSiteKey: String
        get() = sp.getString(KEY_TRAINED_DATA_SITE_KEY, null)
                ?: DEFAULT_TRAINED_DATA_SITE_KEY
        @SuppressLint("ApplySharedPref")
        set(value) {
            if (RemoteConfigUtil.trainedDataSites.any { value == it.key }) {
                sp.edit().putString(KEY_TRAINED_DATA_SITE_KEY, value).commit()
                EventUtil.post(TrainedDataDownloadSiteChangedEvent(trainedDataDownloadSiteIndex))
            }
            UserInfoUtils.updateClientSettings()
        }

    var trainedDataDownloadSiteIndex: Int
        get() = trainedDataSites.indexOfFirst { it.key == trainedDataDownloadSiteKey }
                .let { if (it < 0) 0 else it }
        set(value) {
            trainedDataDownloadSiteKey = trainedDataSites[value].key
        }

    val trainedDataDownloadSite: TrainedDataSite
        get() = trainedDataSites.first { it.key == trainedDataDownloadSiteKey }

    val trainedDataSites: List<TrainedDataSite>
        get() = RemoteConfigUtil.trainedDataSites

    val tessDataDir: File = tessDataLocation.saveDir

    val tessDataBaseDir: File = tessDataDir.parentFile

    var tessDataLocation: TessDataLocation
        get() {
            sp.getString(KEY_TESS_DATA_LOCATION, null)?.let {
                try {
                    return TessDataLocation.valueOf(it)
                } catch (t: Throwable) {
                    //ignore
                }
            }

            if (!isExternalStorageWritable) {
                return TessDataLocation.INTERNAL_STORAGE
            }

            return if (TessDataLocation.INTERNAL_STORAGE.saveDir.exists()) {
                TessDataLocation.INTERNAL_STORAGE
            } else {
                TessDataLocation.EXTERNAL_STORAGE
            }
        }
        set(value) {
            sp.edit().putString(KEY_TESS_DATA_LOCATION, value.name).apply()
        }

    val isExternalStorageWritable: Boolean =
            Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()

    internal val removableFileDirOrNormalFilesDir: File
        get() {
            val context = CoreApplication.instance
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val externalCacheDirs = context.getExternalFilesDirs(null)
                for (file in externalCacheDirs) {
                    if (file != null) {
                        try {
                            Environment.isExternalStorageRemovable(file)
                            return file
                        } catch (t: Throwable) {
                            //ignore
                        }
                    }
                }
            }

            val externalFilesDir = context.getExternalFilesDir(null)
            return externalFilesDir ?: context.filesDir
        }
}

enum class TessDataLocation(baseDir: File) {
    INTERNAL_STORAGE(CoreApplication.instance.filesDir),
    EXTERNAL_STORAGE(removableFileDirOrNormalFilesDir);

    val saveDir: File =
            File(baseDir.absolutePath + File.separator + "tesseract" +
                    File.separator + "tessdata")
}