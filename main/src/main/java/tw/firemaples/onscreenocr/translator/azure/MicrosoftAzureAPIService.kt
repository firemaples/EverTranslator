package tw.firemaples.onscreenocr.translator.azure

import androidx.annotation.Keep
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface MicrosoftAzureAPIService {
    @Headers(
        "Content-Type: application/json",
    )
    @POST("https://api.cognitive.microsofttranslator.com/translate?api-version=3.0")
    suspend fun translate(
        @Header("Ocp-Apim-Subscription-Key") subscriptionKey: String,
        @Query("to") to: String,
        @Body request: List<TranslateRequest>,
    ): Response<List<TranslateResponse>>

    @Headers(
        "Content-Type: application/x-www-form-urlencoded; charset=UTF-8",
        "Accept-Charset: UTF-8",
    )
    @POST("https://api.cognitive.microsoft.com/sts/v1.0/issueToken")
    suspend fun issueToken(
        @Header("Ocp-Apim-Subscription-Key") subscriptionKey: String,
    ): Response<String>
}

@Keep
data class TranslateRequest(
    @Json(name = "Text")
    val text: String
) {
    companion object {
        fun single(text: String) = listOf(TranslateRequest(text))
    }
}

@Keep
data class TranslateResponse(
    @Json(name = "detectedLanguage")
    val detectedLanguage: DetectedLanguage,
    @Json(name = "translations")
    val translations: List<Translation>,
)

@Keep
data class DetectedLanguage(
    @Json(name = "language")
    val language: String,
    @Json(name = "score")
    val score: Float,
)

@Keep
data class Translation(
    @Json(name = "text")
    val text: String,
    @Json(name = "to")
    val to: String,
)
