package tw.firemaples.onscreenocr.data.repo

import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.recognition.TextRecognitionProviderType
import tw.firemaples.onscreenocr.utils.Constants
import javax.inject.Inject

class RecognitionRepository @Inject constructor() {
    val ocrLanguage: Flow<String>
        get() = AppPref.asLiveData(AppPref::selectedOCRLang).asFlow()

    val ocrProvider: Flow<TextRecognitionProviderType>
        get() = AppPref.asLiveData(AppPref::selectedOCRProviderKey).asFlow()
            .map { key ->
                TextRecognitionProviderType.entries.firstOrNull { it.key == key }
                    ?: Constants.DEFAULT_OCR_PROVIDER
            }
}
