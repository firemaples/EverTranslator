package tw.firemaples.onscreenocr.data.repo

import androidx.lifecycle.asFlow
import com.chibatching.kotpref.livedata.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import javax.inject.Inject

class TranslatorRepository @Inject constructor() {
    val currentProviderType: Flow<TranslationProviderType>
        get() = AppPref.asLiveData(AppPref::selectedTranslationProvider).asFlow()
            .map { TranslationProviderType.fromKey(it) }
    val currentTranslationLang: Flow<String>
        get() = AppPref.asLiveData(AppPref::selectedTranslationLang).asFlow()
}
