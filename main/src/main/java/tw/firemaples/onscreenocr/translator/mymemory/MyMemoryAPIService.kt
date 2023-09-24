package tw.firemaples.onscreenocr.translator.mymemory

import com.google.errorprone.annotations.Keep
import com.google.gson.annotations.SerializedName
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
data class ResponseData(
    @SerializedName("translatedText") val translatedText: String,
    @SerializedName("match") val match: Float?,
)

//@Keep
//data class Match(
//    @SerializedName("id") val id: String,
//    @SerializedName("segment") val segment: String,
//    @SerializedName("translation") val translation: String,
//    @SerializedName("source") val source: String,
//    @SerializedName("target") val target: String,
//    @SerializedName("quality") val quality: Int,
//    @SerializedName("reference") val reference: Any?,
//    @SerializedName("usageCount") val usageCount: Int,
//    @SerializedName("subject") val subject: String?,
//    @SerializedName("createdBy") val createdBy: String?,
//    @SerializedName("lastUpdatedBy") val lastUpdatedBy: String?,
//    @SerializedName("createDate") val createDate: String,
//    @SerializedName("lastUpdateDate") val lastUpdateDate: String,
//    @SerializedName("match") val match: Float,
//)

@Keep
data class TranslateResponse(
    @SerializedName("responseData") val responseData: ResponseData,
    @SerializedName("quotaFinished") val quotaFinished: Boolean?,
    @SerializedName("mtLangSupported") val mtLangSupported: Any?,
    @SerializedName("responseDetails") val responseDetails: String,
    @SerializedName("responseStatus") val responseStatus: Any?, // this field can be a number or string
    @SerializedName("responderId") val responderId: Any?,
    @SerializedName("exceptionCode") val exceptionCode: Any?,
//   @SerializedName("matches") val matches: List<Match>, // this field can be an empty string while the API failed
) {
    fun isSuccess(): Boolean = responseStatus.toString().toDoubleOrNull() == 200.0
}
