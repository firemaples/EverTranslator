package tw.firemaples.onscreenocr.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tw.firemaples.onscreenocr.utils.Utils
import java.io.File

object ApiHub {
    private val context: Context by lazy { Utils.context }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val tessDataTempFile: File by lazy {
        File(context.cacheDir, "tess_data_tmp")
    }

    val tessDataDownloader: TessDataDownloader by lazy {
        retrofit.create(TessDataDownloader::class.java)
    }
}
