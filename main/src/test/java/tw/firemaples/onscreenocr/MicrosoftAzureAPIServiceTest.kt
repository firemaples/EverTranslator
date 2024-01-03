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

    private lateinit var key: String

    @Test
    fun `test ms key 1`() {
        setupKey(1)
        testTranslate()
    }

    @Test
    fun `test ms key 2`() {
        setupKey(2)
        testTranslate()
    }

    private fun setupKey(n: Int){
        key = System.getenv().getOrDefault("MS_KEY_$n", "")
    }

    private fun testTranslate() = runTest {
        val result = apiService.translate(
            subscriptionKey = key,
            to = "fr",
            request = TranslateRequest.single("Hello"),
        )

        if (!result.isSuccessful)
            throw Error(result.errorBody()?.toString())

        val actual = result.body()?.firstOrNull()?.translations?.firstOrNull()?.text
        assertEquals("Bonjour", actual)
    }
}
