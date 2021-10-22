package tw.firemaples.onscreenocr

import android.app.Application
import com.androidnetworking.AndroidNetworking
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.FirebaseApp
import okhttp3.OkHttpClient
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.log.UserInfoUtils
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.tts.AndroidTTSManager

class CoreApplication : Application() {
    companion object {
        @JvmStatic
        lateinit var instance: CoreApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        RemoteConfigUtil.tryFetchNew()

        UserInfoUtils.setClientInfo()

        FirebaseApp.initializeApp(this)

        initFastAndroidNetworking()

        FirebaseEvent.validateSignature()

        AndroidTTSManager.getInstance(this).init()
    }

    private fun initFastAndroidNetworking() {
        val okHttpClient = OkHttpClient().newBuilder()
                .addNetworkInterceptor(StethoInterceptor())
                .build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)
    }
}