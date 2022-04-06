package tw.firemaples.onscreenocr.api

import android.content.Context
import retrofit2.Retrofit
import tw.firemaples.onscreenocr.utils.Utils
import java.io.File

object ApiHub {
    private val context: Context by lazy { Utils.context }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("http://localhost/").build()
    }

    val tessDataTempFile: File by lazy {
        File(context.cacheDir, "tess_data_tmp")
    }

    val tessDataDownloader: TessDataDownloader by lazy {
        retrofit.create(TessDataDownloader::class.java)
    }
}
