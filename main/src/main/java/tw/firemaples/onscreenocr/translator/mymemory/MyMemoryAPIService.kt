package tw.firemaples.onscreenocr.translator.mymemory

import androidx.annotation.Keep
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryAPIService {
    @GET("https://api.mymemory.translated.net/get")
    suspend fun translate(
        @Query("langpair") langPair: String,
        @Query("de") email: String? = null,
        @Query("q") text: String,
    ): Response<TranslateResponse>
}

@Keep
data class TranslateResponse(
    @Json(name = "responseData")
    val responseData: ResponseData,
    @Json(name = "quotaFinished")
    val quotaFinished: Boolean?,
    @Json(name = "mtLangSupported")
    val mtLangSupported: Any?,
    @Json(name = "responseDetails")
    val responseDetails: String,
    @Json(name = "responseStatus")
    val responseStatus: Any?, // this field can be a number or string
    @Json(name = "responderId")
    val responderId: Any?,
    @Json(name = "exceptionCode")
    val exceptionCode: Any?,
//   @Json(name = "matches") val matches: List<Match>, // this field can be an empty string while the API failed
) {
    fun isSuccess(): Boolean = responseStatus.toString().toDoubleOrNull() == 200.0
}

@Keep
data class ResponseData(
    @Json(name = "translatedText")
    val translatedText: String,
    @Json(name = "match")
    val match: Float?,
)

//@Keep
//data class Match(
//    @Json(name = "id") val id: String,
//    @Json(name = "segment") val segment: String,
//    @Json(name = "translation") val translation: String,
//    @Json(name = "source") val source: String,
//    @Json(name = "target") val target: String,
//    @Json(name = "quality") val quality: Int,
//    @Json(name = "reference") val reference: Any?,
//    @Json(name = "usageCount") val usageCount: Int,
//    @Json(name = "subject") val subject: String?,
//    @Json(name = "createdBy") val createdBy: String?,
//    @Json(name = "lastUpdatedBy") val lastUpdatedBy: String?,
//    @Json(name = "createDate") val createDate: String,
//    @Json(name = "lastUpdateDate") val lastUpdateDate: String,
//    @Json(name = "match") val match: Float,
//)
