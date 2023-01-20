package tw.firemaples.onscreenocr

import android.app.Application
import com.google.android.gms.ads.MobileAds
import org.opencv.android.OpenCVLoader
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.log.UserInfoUtils
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.AdManager

class CoreApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        FirebaseEvent.validateSignature()
        UserInfoUtils.setClientInfo()
        RemoteConfigManager.tryFetchNew()
        AdManager.init()
        OpenCVLoader.initDebug()

    }
}
