package tw.firemaples.onscreenocr

import android.app.Application
import android.util.Log
import com.androidnetworking.AndroidNetworking
import com.crashlytics.android.Crashlytics
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.firebase.FirebaseApp
import io.fabric.sdk.android.Fabric
import okhttp3.OkHttpClient
import tw.firemaples.onscreenocr.tts.AndroidTTSManager
import tw.firemaples.onscreenocr.utils.FabricUtils
import tw.firemaples.onscreenocr.utils.SignatureUtil

class CoreApplication : Application() {
    companion object {
        @JvmStatic
        lateinit var instance: CoreApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        if (!Fabric.isInitialized()) {
            Fabric.with(this, Crashlytics())
        }

        FabricUtils.setClientInfo(this)

        FirebaseApp.initializeApp(this)

        initFastAndroidNetworking()

        validateSignature()

        AndroidTTSManager.getInstance(this).init()
    }

    private fun validateSignature() {
        try {
            val sha = SignatureUtil.getCurrentSignatureSHA(this)
            val result = SignatureUtil.validateSignature(this)
            Crashlytics.setString("Signature_SHA", sha)
            Crashlytics.setBool("Signature_SHA_is_correct", result)
            Crashlytics.setString("Package_name", packageName)
        } catch (e: Throwable) {
            e.printStackTrace()
            Crashlytics.setString("ValidateSignatureFailed", e.message)
            Crashlytics.log(Log.getStackTraceString(e))
        }

    }

    private fun initFastAndroidNetworking() {
        val okHttpClient = OkHttpClient().newBuilder()
                .addNetworkInterceptor(StethoInterceptor())
                .build()
        AndroidNetworking.initialize(applicationContext, okHttpClient)
    }
}