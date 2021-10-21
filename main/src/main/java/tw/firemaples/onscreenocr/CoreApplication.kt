package tw.firemaples.onscreenocr

import android.app.Application
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager

class CoreApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        RemoteConfigManager.tryFetchNew()
    }
}
