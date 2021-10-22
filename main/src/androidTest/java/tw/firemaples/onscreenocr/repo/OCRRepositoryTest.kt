package tw.firemaples.onscreenocr.repo

import junit.framework.TestCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import tw.firemaples.onscreenocr.pref.AppPref

class OCRRepositoryTest : TestCase() {

    private val repo = OCRRepository()

    public override fun setUp() {
        super.setUp()
    }

    public override fun tearDown() {}

    fun testGetSelectedOCRLangFlow() = runBlocking {
        val lang = "zh"
        repo.setSelectedOCRLanguage(lang)

        assertEquals(lang, repo.selectedOCRLangFlow.first())
    }

    fun testSetSelectedOCRLanguage() = runBlocking {
        val lang = "zh"
        repo.setSelectedOCRLanguage(lang)

        assertEquals(lang, AppPref.selectedOCRLang)
    }

    fun testGetAllOCRLanguages() = runBlocking {
        assertTrue(repo.getAllOCRLanguages().first().isNotEmpty())
    }
}