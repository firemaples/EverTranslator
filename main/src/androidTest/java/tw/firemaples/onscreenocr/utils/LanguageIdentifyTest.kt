package tw.firemaples.onscreenocr.utils

import junit.framework.TestCase
import kotlinx.coroutines.runBlocking

class LanguageIdentifyTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    public override fun tearDown() {}

    fun testIdentifyLanguageEnglish() = runBlocking {
        val result = LanguageIdentify.identifyLanguage("Test code")
        assertEquals("en", result)
    }

    fun testIdentifyLanguageChinese() = runBlocking {
        val result = LanguageIdentify.identifyLanguage("測試")
        assertEquals("zh", result)
    }

    fun testIdentifyLanguageJapanese() = runBlocking {
        val result = LanguageIdentify.identifyLanguage("やった!")
        assertEquals("ja", result)
    }

    fun testIdentifyLanguageKorean() = runBlocking {
        val result = LanguageIdentify.identifyLanguage("한국어")
        assertEquals("ko", result)
    }

    fun testIdentifyLanguageDevanagari() = runBlocking {
        val result = LanguageIdentify.identifyLanguage("देवनागरी")
        assertEquals("ne", result)
    }
}