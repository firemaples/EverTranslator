package tw.firemaples.onscreenocr.recognition

import android.content.Context
import android.graphics.Bitmap
import com.googlecode.tesseract.android.TessBaseAPI
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.repo.OCRRepository
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import java.io.File

class TesseractTextRecognizer : TextRecognizer {
    companion object {
        private const val TRAINED_DATA_FILE_NAME_SUFFIX = ".traineddata"

        private val logger: Logger by lazy { Logger(TesseractTextRecognizer::class) }
        private val context: Context by lazy { Utils.context }

        private val tessBaseDir: File
            get() = File(context.getExternalFilesDir(null) ?: context.filesDir, "tesseract")
        private val tessDataDir: File
            get() = File(tessBaseDir, "tessdata")

        private fun initTessDataFolder(): Boolean =
            (tessDataDir.isDirectory || tessDataDir.mkdirs()).also {
                if (!it) logger.warn("Initializing tesseract data folder failed")
            }

        fun getTessDataFile(langCode: String): File =
            File(tessDataDir, "$langCode$TRAINED_DATA_FILE_NAME_SUFFIX")

        private fun getDownloadedLangCodes(): Set<String> {
            if (!initTessDataFolder()) {
                return emptySet()
            }

            return tessDataDir.list { _, name -> name.endsWith(TRAINED_DATA_FILE_NAME_SUFFIX) }
                ?.map { it.substring(0, it.length - TRAINED_DATA_FILE_NAME_SUFFIX.length) }
                ?.toHashSet() ?: emptySet()
        }

        fun getSupportedLanguageList(context: Context): List<RecognitionLanguage> {
            val res = context.resources
            val innerCodes = res.getStringArray(R.array.lang_ocr_tesseract_code_iso6393)
            val displayCodes = res.getStringArray(R.array.lang_ocr_tesseract_display_code_iso6391)
            val names = res.getStringArray(R.array.lang_ocr_tesseract_name)
            val downloadedCodes = getDownloadedLangCodes()

            return innerCodes.indices
                .mapNotNull { i ->
                    val displayCode = displayCodes[i]
                    val name = names[i]
                    val innerCode = innerCodes[i]

                    RecognitionLanguage(
                        code = displayCode,
                        displayName = name,
                        selected = false, //displayCode == selected,
                        downloaded = downloadedCodes.contains(innerCode),
                        recognizer = Recognizer.Tesseract,
                        innerCode = innerCode,
                    )
                }
                .sortedBy { it.displayName }
        }
    }

//    private val ocrRepo: OCRRepository by lazy { OCRRepository() }

    override val type: Recognizer
        get() = Recognizer.Tesseract
    override val name: String
        get() = type.name

    override suspend fun recognize(lang: RecognitionLanguage, bitmap: Bitmap): RecognitionResult {
        val tess = TessBaseAPI()
        tess.init(tessBaseDir.absolutePath, lang.innerCode)
        tess.setImage(bitmap)
        val resultText = tess.utF8Text
        val boxes = tess.regions.boxRects

        return RecognitionResult("en", resultText, boxes)
    }

//    @Suppress("RedundantSuspendModifier")
//    suspend fun isTessDataExists(langCode: String): Boolean {
//        if (!initTessDataFolder()) {
//            return false
//        }
//
//        return getTessDataFile(langCode).exists()
//    }
//
//    suspend fun downloadTessLangData(langCode: String): Boolean {
//        val file = getTessDataFile(langCode)
//        if (ocrRepo.downloadTessData(langCode, file)) {
//            return true
//        }
//        return false
//    }

//    override suspend fun supportedLanguages(): List<RecognitionLanguage> {
//        return emptyList()
//    }

    override suspend fun parseToDisplayLangCode(langCode: String): String {
        return langCode
    }
}
