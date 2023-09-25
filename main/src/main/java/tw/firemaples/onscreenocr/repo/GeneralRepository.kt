package tw.firemaples.onscreenocr.repo

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.floatings.readme.ReadmeView
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils

class GeneralRepository {
    companion object {
        private const val FORMAT_VERSION_MESSAGE_KEY = "version_%s"
    }

    private val logger: Logger by lazy { Logger(GeneralRepository::class) }
    private val context: Context by lazy { Utils.context }

    fun isRememberLastSelection(): Flow<Boolean> = flow {
        emit(SettingManager.saveLastSelectionArea)
    }.flowOn(Dispatchers.Default)

    fun getLastRememberedSelectionArea(): Flow<Rect?> = flow {
        emit(AppPref.lastSelectionArea)
    }.flowOn(Dispatchers.Default)

    suspend fun setLastRememberedSelectionArea(rect: Rect) {
        withContext(Dispatchers.Default) {
            AppPref.lastSelectionArea = rect
        }
    }

    fun isReadmeAlreadyShown(): Flow<Boolean> = flow {
        val lastVersionName = ReadmeView.VERSION
        val lastShownName = AppPref.lastReadmeShownVersion

        if (lastVersionName != lastShownName) {
            AppPref.lastReadmeShownVersion = lastVersionName
            emit(false)
        } else {
            emit(true)
        }
    }

    fun showVersionHistory(): Flow<Boolean> = flow {
        val lastShownName = AppPref.lastVersionHistoryShownVersion
        val version = Utils.getPackageInfo(context.packageName)?.versionName

        if (lastShownName != version) {
            AppPref.lastVersionHistoryShownVersion = version
            emit(true)
        } else {
            emit(false)
        }
    }

    fun saveLastMainBarPosition(x: Int, y: Int) {
        AppPref.lastMainBarPosition = Point(x, y)
    }

    fun isAutoCopyOCRResult(): Flow<Boolean> = flow {
        emit(SettingManager.autoCopyOCRResult)
    }.flowOn(Dispatchers.Default)

    fun hideRecognizedTextAfterTranslated(): Flow<Boolean> = flow {
        emit(SettingManager.hideRecognizedResultAfterTranslated)
    }.flowOn(Dispatchers.Default)

    data class Record(val version: String, val desc: String)
}
