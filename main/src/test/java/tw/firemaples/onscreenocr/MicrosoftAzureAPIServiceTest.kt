package tw.firemaples.onscreenocr

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import tw.firemaples.onscreenocr.translator.azure.MicrosoftAzureAPIService
import tw.firemaples.onscreenocr.translator.azure.TranslateRequest

class MicrosoftAzureAPIServiceTest {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://localhost/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val apiService: MicrosoftAzureAPIService by lazy {
        retrofit.create(MicrosoftAzureAPIService::class.java)
    }

    @Test
    fun test() = runTest {
        val key = System.getenv().getOrDefault("MS_KEY_1", "")

        val result = apiService.translate(
            subscriptionKey = key,
            to = "fr",
            request = TranslateRequest.single("Hello"),
        )

        val actual = result.body()?.firstOrNull()?.translations?.firstOrNull()?.text
        assertEquals("Bonjour", actual)
    }
}
