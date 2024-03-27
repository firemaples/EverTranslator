package tw.firemaples.onscreenocr.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.webkit.WebView
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tw.firemaples.onscreenocr.BuildConfig
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.R

object Utils {
    val logger: Logger by lazy { Logger(Utils::class) }

    val context: Context by lazy { CoreApplication.instance }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .apply {
                if (!BuildConfig.DISABLE_LOGGING) {
                    addInterceptor(HttpLoggingInterceptor { msg ->
                        logger.debug(msg)
                    }.apply {
                        setLevel(HttpLoggingInterceptor.Level.BODY)
                    })
                }
            }.build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun isPackageInstalled(packageName: String): Boolean =
        getPackageInfo(packageName) != null

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(packageName: String): PackageInfo? =
        try {
            context.packageManager.getPackageInfo(packageName, 0)
        } catch (e: Exception) {
            logger.warn(t = e)
            null
        }

    fun batteryOptimized(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(context.packageName)?.not() ?: false
        } else false
    }

    fun openBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            logger.warn("Unable to open a URL in browser", e)
        }
    }

    fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        shareIntent.flags += Intent.FLAG_ACTIVITY_NEW_TASK

        try {
            ContextCompat.startActivity(context, shareIntent, null)
        } catch (e: Exception) {
            logger.warn("Share text failed", e)
        }
    }

    fun copyToClipboard(label: String, text: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let {
            it.setPrimaryClip(ClipData.newPlainText(label, text))
            Toaster.show(String.format(context.getString(R.string.msg_textHasBeenCopied), text))
        }
    }
}

fun String.firstPart(): String = split(":")[0].split("-")[0]

private val googleTranslateLang = mapOf(
    "zh-TW" to setOf("zh-tw", "zh-hant", "zh"),
    "zh-CN" to setOf("zh-cn", "zh-hans"),
)

fun String.toGoogleTranslateLang(): String {
    val target = this
    googleTranslateLang.entries.forEach { (lang, set) ->
        if (set.contains(target.lowercase())) return lang
    }
    return target.firstPart()
}

fun Context.getThemedLayoutInflater(theme: Int = R.style.Theme_EverTranslator): LayoutInflater =
    LayoutInflater.from(this)
        .cloneInContext(ContextThemeWrapper(this, theme))

fun WebView.setAutoDarkMode() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
            try {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true)
            } catch (e: Exception) {
                Utils.logger.warn(t = e)
            }
        }
    } else {
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON)
                }

                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_OFF)
                }

                else -> {
                    //
                }
            }
        }
    }
}
