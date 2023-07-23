package tw.firemaples.onscreenocr.repo

import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType

class OCRRepositoryTest : TestCase() {

    private val repo = OCRRepository()

    public override fun setUp() {
        super.setUp()
    }

    fun testSetSelectedOCRLanguage() = runBlocking {
        val lang = "zh"
        repo.setSelectedOCRLanguage(lang, TextRecognitionProviderType.GoogleMLKit)

        assertEquals(lang, AppPref.selectedOCRLang)
    }

    fun testGetAllOCRLanguages() = runBlocking {
        assertTrue(repo.getAllOCRLanguages().first().isNotEmpty())
    }
}