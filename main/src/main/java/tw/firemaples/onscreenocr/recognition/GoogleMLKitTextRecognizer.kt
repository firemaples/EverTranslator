package tw.firemaples.onscreenocr.recognition

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.pages.setting.SettingManager
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Utils
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GoogleMLKitTextRecognizer : TextRecognizer {
    companion object {
        private val devanagariLangCodes = arrayOf("hi", "mr", "ne", "sa")

        fun getSupportedLanguageList(context: Context): List<RecognitionLanguage> {
            val res = context.resources
            val langCodes = res.getStringArray(R.array.lang_ocr_google_mlkit_code_bcp_47)
            val langNames = res.getStringArray(R.array.lang_ocr_google_mlkit_name)

            return langCodes.indices
                .mapNotNull { i ->
                    val name = langNames[i]

                    if (name.startsWith("old ", ignoreCase = true) ||
                        name.startsWith("middle ", ignoreCase = true)
                    ) null
                    else {
                        val code = langCodes[i]
                        RecognitionLanguage(
                            code = code,
                            displayName = name,
                            selected = false,
                            downloaded = true,
                            recognizer = TextRecognitionProviderType.GoogleMLKit,
                            innerCode = code,
                        )
                    }
                }
                .distinctBy { it.displayName }
                .sortedBy { it.displayName }
        }
    }

    private val context: Context by lazy { Utils.context }

    override val type: TextRecognitionProviderType
        get() = TextRecognitionProviderType.GoogleMLKit

    override val name: String
        get() = type.name

    private val recognizerMap =
        mutableMapOf<ScriptType, com.google.mlkit.vision.text.TextRecognizer>()

    override suspend fun recognize(lang: RecognitionLanguage, bitmap: Bitmap): RecognitionResult {
        val lang = AppPref.selectedOCRLang
        return doRecognize(bitmap, lang)
    }

    private suspend fun doRecognize(bitmap: Bitmap, lang: String): RecognitionResult =
        suspendCoroutine {
            val script = getScriptType(lang)

            val recognizer = recognizerMap.getOrPut(script) {
                FirebaseEvent.logStartOCRInitializing(name)

                val options = when (script) {
                    ScriptType.Chinese -> ChineseTextRecognizerOptions.Builder().build()
                    ScriptType.Devanagari -> DevanagariTextRecognizerOptions.Builder().build()
                    ScriptType.Japanese -> JapaneseTextRecognizerOptions.Builder().build()
                    ScriptType.Korean -> KoreanTextRecognizerOptions.Builder().build()
                    ScriptType.Latin -> TextRecognizerOptions.DEFAULT_OPTIONS
                }

                TextRecognition.getClient(options).also {
                    FirebaseEvent.logOCRInitialized(name)
                }
            }

            val image = InputImage.fromBitmap(bitmap, 0)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val joiner = SettingManager.textBlockJoiner.joiner
                    var text = result.textBlocks
                        .joinToString(separator = joiner) {
                            if (SettingManager.removeLineBreakersInBlock) {
                                it.text
                                    .replace("\n ", " ")
                                    .replace("\n", " ")
                            } else it.text
                        }
                    if (SettingManager.removeEndDash) {
                        text = text.replace("-\n", "")
                    }

                    it.resume(
                        RecognitionResult(
                            langCode = lang.toISO639(),
                            result = text,
                            boundingBoxes = result.textBlocks.mapNotNull { it.boundingBox }),
                    )
                }
                .addOnFailureListener { e ->
                    it.resumeWithException(e)
                }
        }

    override suspend fun parseToDisplayLangCode(langCode: String): String = langCode.toISO639()

//    override suspend fun supportedLanguages(): List<RecognitionLanguage> {
//        return getSupportedLanguageList()
//    }
//
//    private fun getSupportedLanguageList(): List<RecognitionLanguage> {
//        val res = context.resources
//        val langCodes = res.getStringArray(R.array.lang_ocr_google_mlkit_code_bcp_47)
//        val langNames = res.getStringArray(R.array.lang_ocr_google_mlkit_name)
//        val selected = AppPref.selectedOCRLang.let {
//            if (langCodes.contains(it)) it else Constants.DEFAULT_OCR_LANG
//        }
//
//        return langCodes.indices
//            .map { i ->
//                val code = langCodes[i]
//                RecognitionLanguage(
//                    code = code,
//                    displayName = langNames[i],
//                    selected = code == selected,
//                    downloaded = true,
//                    recognizer = Recognizer.GoogleMLKit,
//                    innerCode = code,
//                )
//            }
//            .sortedBy { it.displayName }
//            .distinctBy { it.displayName }
//            .filterNot {
//                it.displayName.startsWith("old ", ignoreCase = true) ||
//                        it.displayName.startsWith("middle ", ignoreCase = true)
//            }
//    }

//    private fun getSupportedScriptList(): List<RecognitionLanguage> {
//        val res = context.resources
//        val scriptCodes = res.getStringArray(R.array.google_MLKit_translationScriptCode)
//        val scriptNames = res.getStringArray(R.array.google_MLKit_translationScriptName)
//        val selected = AppPref.selectedOCRLang.let {
//            if (scriptCodes.contains(it)) it else scriptCodes[0]
//        }
//
//        return scriptCodes.indices.map { i ->
//            val code = scriptCodes[i]
//            RecognitionLanguage(
//                code = code,
//                displayName = scriptNames[i],
//                selected = code == selected,
//                downloaded = true,
//                recognizer = Recognizer.GoogleMLKit,
//                innerCode = code,
//            )
//        }
//    }

    private fun getScriptType(lang: String): ScriptType =
        when {
            ScriptType.Japanese.isJapanese(lang) -> ScriptType.Japanese
            ScriptType.Korean.isKorean(lang) -> ScriptType.Korean
            ScriptType.Chinese.isChinese(lang) -> ScriptType.Chinese
            ScriptType.Devanagari.isDevanagari(lang) -> ScriptType.Devanagari
            else -> ScriptType.Latin
        }

    private sealed class ScriptType {
        object Latin : ScriptType()
        object Chinese : ScriptType() {
            fun isChinese(lang: String): Boolean = lang.startsWith("zh")
        }

        object Devanagari : ScriptType() {
            fun isDevanagari(lang: String): Boolean = devanagariLangCodes.contains(lang)
        }

        object Japanese : ScriptType() {
            fun isJapanese(lang: String): Boolean = lang == "ja"
        }

        object Korean : ScriptType() {
            fun isKorean(lang: String): Boolean = lang == "ko"
        }
    }

    private fun String.toISO639(): String = split("-")[0]
}
