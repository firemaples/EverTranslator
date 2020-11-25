package tw.firemaples.onscreenocr.remoteconfig

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.remoteconfig.data.TrainedDataFileNames
import tw.firemaples.onscreenocr.utils.JsonUtil
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.TypeReference

internal const val KEY_VERSION = "version"
internal const val KEY_FETCH_INTERVAL = "fetch_interval"
internal const val KEY_MICROSOFT_KEY = "microsoft_key"
internal const val KEY_MICROSOFT_KEY_GROUP_ID = "microsoft_key_group_id"
internal const val KEY_TRAINED_DATA_URL = "trained_data_url_data_v1"
internal const val KEY_TRAINED_DATA_FILES = "trained_data_files"
internal const val KEY_PRIVACY_POLICY_URL = "privacy_policy_url"

object RemoteConfigUtil {
    private val logger: Logger = LoggerFactory.getLogger(RemoteConfigUtil::class.java)

    private val context: Context by lazy { CoreApplication.instance }

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        val fetchInterval = if (BuildConfig.DEBUG) 0 else SettingUtil.firebaseRemoteConfigFetchInterval
        val settings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(fetchInterval).build()

        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(settings)
        }
    }

    fun tryFetchNew() {
        logger.info("fetchTimeMillis: ${remoteConfig.info.fetchTimeMillis}")
        logger.info("lastFetchStatus: ${getLastFetchState()}")

        logger.info("Version before fetch: $versionString, keyGroup: $microsoftTranslationKeyGroupId")
        remoteConfig.fetchAndActivate().addOnSuccessListener {
            logger.info("Version after fetch: $versionString, keyGroup: $microsoftTranslationKeyGroupId")

            SettingUtil.firebaseRemoteConfigFetchInterval = remoteConfig.getLong(KEY_FETCH_INTERVAL)
        }.addOnFailureListener {
            logger.error("Remote config fetch failed", it)
        }
    }

    private fun getLastFetchState(): String =
            when (remoteConfig.info.lastFetchStatus) {
                FirebaseRemoteConfig.LAST_FETCH_STATUS_NO_FETCH_YET -> "NotFetchYet"
                FirebaseRemoteConfig.LAST_FETCH_STATUS_SUCCESS -> "Success"
                FirebaseRemoteConfig.LAST_FETCH_STATUS_FAILURE -> "Failure"
                FirebaseRemoteConfig.LAST_FETCH_STATUS_THROTTLED -> "Throttled"
                else -> "Unknown"
            }

    private fun getString(key: String): String =
            remoteConfig.getString(key)

    private fun getStringsByPrefix(prefix: String): Set<String> =
            remoteConfig.getKeysByPrefix(prefix)

    private val versionString: String
        get() = getString(KEY_VERSION)

    val microsoftTranslationKeyGroupId: String
        get() = getString(KEY_MICROSOFT_KEY_GROUP_ID)

    val microsoftTranslationKey: String
        get() = getString(KEY_MICROSOFT_KEY).let {
            if (it.isBlank()) context.getString(R.string.default_microsoft_key) else it
        }

    val trainedDataSites: List<TrainedDataSite>
        get() = JsonUtil<List<TrainedDataSite>>()
                .parseJson(getString(KEY_TRAINED_DATA_URL),
                        object : TypeReference<List<TrainedDataSite>>() {}) ?: listOf()

    fun trainedDataFileSubs(ocrLang: String): Array<String> {
        val fileSubNameString = getString(KEY_TRAINED_DATA_FILES)
        val fileSubNames = JsonUtil<TrainedDataFileNames>()
                .parseJson(fileSubNameString, object : TypeReference<TrainedDataFileNames>() {})

        val subNames = mutableListOf<String>()
        subNames.addAll(fileSubNames.default)
        fileSubNames.others[ocrLang]?.also {
            subNames.addAll(it)
        }

        return subNames.toTypedArray()
    }

    val privacyPolicyUrl: String
        get() = getString(KEY_PRIVACY_POLICY_URL)
}

data class TrainedDataSite(val name: String, val key: String, val url: String)