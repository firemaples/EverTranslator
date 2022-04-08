//package tw.firemaples.onscreenocr.utils
//
//import android.content.Context
//import androidx.appcompat.app.AppCompatActivity
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.LifecycleObserver
//import androidx.lifecycle.LifecycleOwner
//import androidx.lifecycle.OnLifecycleEvent
//import com.mopub.common.MoPub
//import com.mopub.common.SdkConfiguration
//import com.mopub.common.logging.MoPubLog
//import com.mopub.mobileads.MoPubErrorCode
//import com.mopub.mobileads.MoPubView
//import tw.firemaples.onscreenocr.BuildConfig
//import tw.firemaples.onscreenocr.R
//import tw.firemaples.onscreenocr.log.FirebaseEvent
//import java.util.*
//
//object MoPubAdManager {
//    private val logger: Logger by lazy { Logger(MoPubAdManager::class) }
//
//    private val enableAds: Boolean by lazy { BuildConfig.ENABLE_ADS }
//
//    private var initialized: Boolean = false
//    private var initializing: Boolean = false
//
//    private val adRequestTasks = mutableListOf<(() -> Unit)>()
//
//    private fun init(context: Context, onInitialized: () -> Unit) {
//        val configuration =
//            SdkConfiguration.Builder(context.getString(R.string.mopub_ad_unit_id_permission_page_banner))
//                .also {
//                    if (BuildConfig.DEBUG) {
//                        it.withLogLevel(MoPubLog.LogLevel.DEBUG)
//                    } else {
//                        it.withLogLevel(MoPubLog.LogLevel.INFO)
//                    }
//                }
//                .build()
//
//        MoPub.initializeSdk(context, configuration) {
//            onInitialized.invoke()
//        }
//    }
//
//    fun loadPermissionPageBanner(activity: AppCompatActivity, moPubView: MoPubView) {
//        if (!enableAds) return
//
//        val adUnitId = activity.getString(R.string.mopub_ad_unit_id_permission_page_banner)
//        if (adUnitId.isBlank()) return
//
//        afterInitialized(activity) {
//            moPubView.setAdUnitId(adUnitId)
//            moPubView.bannerAdListener = bannerAdListener
//            moPubView.setupAndLoad(activity)
//        }
//    }
//
//    fun loadSettingPageBanner(activity: AppCompatActivity, moPubView: MoPubView) {
//        if (!enableAds) return
//
//        val adUnitId = activity.getString(R.string.mopub_ad_unit_id_setting_page_banner)
//        if (adUnitId.isBlank()) return
//
//        afterInitialized(activity) {
//            moPubView.setAdUnitId(adUnitId)
//            moPubView.bannerAdListener = bannerAdListener
//            moPubView.setupAndLoad(activity)
//        }
//    }
//
//    private fun afterInitialized(context: Context, task: () -> Unit) {
//        if (initialized) task.invoke()
//        else {
//            synchronized(this) {
//                if (initialized) task.invoke()
//                else {
//                    adRequestTasks.add(task)
//                    if (!initializing) {
//                        initializing = true
//                        init(context) {
//                            synchronized(this) {
//                                initialized = true
//                                initializing = false
//                                adRequestTasks.forEach { it.invoke() }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private fun MoPubView.setupAndLoad(lifecycleOwner: LifecycleOwner) {
//        loadAd()
//
//        lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
//            @Suppress("unused")
//            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
//            fun onDestroy() {
//                this@setupAndLoad.destroy()
//            }
//        })
//    }
//
//    private val bannerAdListener = object : MoPubView.BannerAdListener {
//        override fun onBannerLoaded(view: MoPubView) {
//            logger.debug("onBannerLoaded(), unitId: ${view.getAdUnitId()}")
//
//            val adUnitId = view.getAdUnitId() ?: return
//            FirebaseEvent.logEventAdShowSuccess(adUnitId)
//        }
//
//        override fun onBannerFailed(view: MoPubView?, errorCode: MoPubErrorCode?) {
//            logger.warn("onBannerFailed(), errorCode: $errorCode, unitId: ${view?.getAdUnitId()}")
//
//            val adUnitId = view?.getAdUnitId() ?: return
//            val error = errorCode?.name ?: "NULL"
//            FirebaseEvent.loadEventAdShowFailed(adUnitId, error)
//        }
//
//        override fun onBannerClicked(view: MoPubView?) {
//        }
//
//        override fun onBannerExpanded(view: MoPubView?) {
//        }
//
//        override fun onBannerCollapsed(view: MoPubView?) {
//        }
//    }
//}