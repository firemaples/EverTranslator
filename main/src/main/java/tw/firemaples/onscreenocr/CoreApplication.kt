package tw.firemaples.onscreenocr

import android.app.Application

class CoreApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
