package tw.firemaples.onscreenocr.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

interface TessDataDownloader {
    @Streaming
    @GET("https://github.com/firemaples/tessdata/blob/tag/4.0.0/{lang}.traineddata?raw=true")
    fun downloadFromGithub(@Path("lang") lang: String): Call<ResponseBody>
}
