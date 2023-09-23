package tw.firemaples.onscreenocr.translator.azure

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigManager
import tw.firemaples.onscreenocr.utils.Logger

object MicrosoftAzureTranslatorAPI {
    private val logger: Logger by lazy { Logger(this::class) }
    private val dateFormat = SimpleDateFormat("mm/dd HH:mm:ss", Locale.US)

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val apiService: MicrosoftAzureAPIService by lazy {
        retrofit.create(MicrosoftAzureAPIService::class.java)
    }

    private var token: String = ""
    private var expiredTime: Long = 0L

    @Throws(IllegalStateException::class)
    private suspend fun getToken(): String {
        if (System.currentTimeMillis() >= expiredTime) {
            val result = apiService.issueToken(RemoteConfigManager.microsoftTranslationKey)
            if (result.isSuccessful) {
                val t = result.body()
                if (t != null) {
                    token = "Bearer $t"
                    expiredTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)
                    logger.debug(
                        "token retrieved: $token, expiredTime: ${dateFormat.format(expiredTime)}"
                    )
                } else {
                    logger.error("Got null token: $result")
                    error("Get new token failed")
                }
            } else {
                logger.error("Getting token failed: $result")
                error("Getting token failed")
            }
        }

        return token
    }

    @Throws(IllegalStateException::class, IOException::class)
    suspend fun translate(text: String, targetLang: String): String {
        val result = apiService.translate(
            subscriptionKey = RemoteConfigManager.microsoftTranslationKey,
            to = targetLang,
            request = TranslateRequest.single(text),
        )

        if (!result.isSuccessful) {
            throw IOException("Getting result failed: ${result.errorBody()?.toString()}")
        }
        val data = result.body() ?: error("Got null body: $result")
        logger.debug("Get translation result: $data")
        return data.firstOrNull()?.translations
            ?.firstOrNull()?.text
            ?: error("Got empty result")
    }
}
