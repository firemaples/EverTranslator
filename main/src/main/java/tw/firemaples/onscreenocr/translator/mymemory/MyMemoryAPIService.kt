package tw.firemaples.onscreenocr.translator.mymemory

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MyMemoryAPIService {
    @GET("https://api.mymemory.translated.net/get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String,
        @Query("de") email: String? = null,
    ): Response<TranslateResponse>
}

data class ResponseData(
    val translatedText: String,
    val match: Float?,
)

//@JsonClass(generateAdapter = true)
//data class Match(
//    val id: String,
//    val segment: String,
//    val translation: String,
//    val source: String,
//    val target: String,
//    val quality: Int,
//    val reference: Any?,
//    val usageCount: Int,
//    val subject: String?,
//    val createdBy: String?,
//    val lastUpdatedBy: String?,
//    val createDate: String,
//    val lastUpdateDate: String,
//    val match: Float,
//)

data class TranslateResponse(
    val responseData: ResponseData,
    val quotaFinished: Boolean?,
    val mtLangSupported: Any?,
    val responseDetails: String,
    val responseStatus: Any, // this field can be a number or string
    val responderId: Any?,
    val exceptionCode: Any?,
//    val matches: List<Match>, // this field can be an empty string while the API failed
) {
    fun isSuccess(): Boolean = responseStatus.toString().toDoubleOrNull() == 200.0
}
