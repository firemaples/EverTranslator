package tw.firemaples.onscreenocr.floatings.compose.langselectpanel

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

interface LanguageSelectionPanelViewModel {
    val state: StateFlow<LanguageSelectionPanelState>
}

@Immutable
data class LanguageSelectionPanelState(
    val ocrLanguageList: List<OCRLanguage> = listOf(),
)

data class OCRLanguage(
    val languageName: String,
    val selected: Boolean,
)

class LanguageSelectionPanelViewModelImpl @Inject constructor(

) : LanguageSelectionPanelViewModel {
    override val state = MutableStateFlow(LanguageSelectionPanelState())


}
