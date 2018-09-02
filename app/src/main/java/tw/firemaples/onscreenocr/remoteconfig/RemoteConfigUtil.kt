package tw.firemaples.onscreenocr.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.utils.SettingUtil

internal const val keyVersion = "version"
internal const val keyFetchInterval = "fetch_interval"
internal const val prefixMicrosoftKey = "microsoft_key_"
internal const val keyTrainedDataUrl = "trained_data_url"

object RemoteConfigUtil {
    private val logger: Logger = LoggerFactory.getLogger(RemoteConfigUtil::class.java)

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        val config = FirebaseRemoteConfig.getInstance()
        config.setConfigSettings(FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build())
        config.setDefaults(R.xml.remote_config_defaults)

        config
    }

    fun tryFetchNew() {
        logger.info("fetchTimeMillis: ${remoteConfig.info.fetchTimeMillis}")
        logger.info("lastFetchStatus: ${getLastFetchState()}")

        logger.info("Version before fetch: $versionString")
        remoteConfig.fetch(fetchInterval).addOnSuccessListener {
            logger.info("Remote config fetch successfully")
            remoteConfig.activateFetched()

            logger.info("Version after fetch: $versionString")
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
        get() = getString(keyVersion)

    private val fetchInterval: Long
        get() = remoteConfig.getLong(keyFetchInterval)

    private val microsoftKeys: Array<String>
        get() = getStringsByPrefix(prefixMicrosoftKey).toTypedArray()

    val microsoftTranslationKey: String
        get() =
            if (microsoftKeys.isNotEmpty()) {
                val keys = microsoftKeys
                val index = Math.abs(SettingUtil.deviceId.hashCode()) % keys.size
                val key = keys[index]
                val msKey = getString(key)
                logger.info("Using Microsoft translator key: ${msKey.substring(0, 5)}($index)")
                msKey
            } else ""

    val trainedDataUrl: String
        get() = getString(keyTrainedDataUrl)
}