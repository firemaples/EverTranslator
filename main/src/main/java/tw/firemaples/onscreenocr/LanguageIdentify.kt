package tw.firemaples.onscreenocr

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object LanguageIdentify {
    private val client: LanguageIdentifier by lazy { LanguageIdentification.getClient() }

    suspend fun identifyLanguage(text: String): String? =
        suspendCoroutine { c ->
            client.identifyLanguage(text)
                .addOnSuccessListener {
                    if (it.equals("und", ignoreCase = true)) {
                        c.resume(null)
                    } else {
                        c.resume(it)
                    }
                }
                .addOnFailureListener {
                    c.resumeWithException(it)
                }
        }
}
