package tw.firemaples.onscreenocr.repo

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Utils

class OCRRepository {
    private val context: Context by lazy { Utils.context }

    fun getAllOCRLanguages(): Flow<List<OCRLanguage>> = flow {
        val res = context.resources
        val codeList = res.getStringArray(R.array.ocr_langCode_iso6391)
        val ocrCodeList = res.getStringArray(R.array.ocr_langCode_iso6393)
        val displayNameList = res.getStringArray(R.array.ocr_langName)

        val selectedLang = AppPref.selectedOCRLang

        val result = (codeList.indices).map {
            val code = codeList[it]
            val ocrCode = ocrCodeList[it]
            val displayName = displayNameList[it]

            OCRLanguage(code, ocrCode, displayName, code == selectedLang)
        }

        emit(result)
    }.flowOn(Dispatchers.Default)

    fun getSelectedOCRLanguage(): Flow<String> = flow {
        emit(AppPref.selectedOCRLang)
    }.flowOn(Dispatchers.Default)

    suspend fun setSelectedOCRLanguage(langCode: String) {
        withContext(Dispatchers.Default) {
            AppPref.selectedOCRLang = langCode
        }
    }
}

data class OCRLanguage(
    val code: String,
    val ocrCode: String,
    val displayName: String,
    val selected: Boolean,
)
