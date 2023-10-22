package tw.firemaples.onscreenocr.utils

import android.os.Build
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref

object DeviceInfoChecker {
    private val logger: Logger by lazy { Logger(this::class) }
    fun check() {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val os = Build.VERSION.SDK_INT

        val deviceInfo = "$manufacturer,$model,$os"
        logger.debug("DeviceInfo: $deviceInfo")

        if (deviceInfo != AppPref.lastDeviceInfo) {
            when {
                manufacturer.contains("Xiaomi", ignoreCase = true) &&
                        os >= Build.VERSION_CODES.S -> {
                    keepMediaProjectionResources()
                }

                manufacturer.contains("Blackshark", ignoreCase = true) &&
                        os >= Build.VERSION_CODES.S -> {
                    keepMediaProjectionResources()
                }

                os >= 34 -> {
                    keepMediaProjectionResources()
                }
            }
        }

        AppPref.lastDeviceInfo = deviceInfo
    }

    private fun keepMediaProjectionResources() {
        logger.debug("keepMediaProjectionResources()")
        SettingManager.keepMediaProjectionResources = true
    }
}
