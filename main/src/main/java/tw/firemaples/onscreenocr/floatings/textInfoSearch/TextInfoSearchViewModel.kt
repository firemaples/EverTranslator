package tw.firemaples.onscreenocr.floatings.textInfoSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import java.net.URLEncoder

class TextInfoSearchViewModel(
    viewScope: CoroutineScope,
    private val text: String,
    private val sourceLang: String
) : FloatingViewModel(viewScope) {
    private val _loadUrl = MutableLiveData<Page>()
    val loadUrl: LiveData<Page> = _loadUrl

    fun onLoad() {
        viewScope.launch { _loadUrl.value = Page.default(text, sourceLang) }
    }

    fun onRefreshClicked() {
        viewScope.launch { _loadUrl.value = _loadUrl.value }
    }

    fun onGoogleTranslateClicked() {
        viewScope.launch { _loadUrl.value = Page.GoogleTranslate(text, sourceLang) }
    }

    fun onGoogleDefinitionClicked() {
        viewScope.launch { _loadUrl.value = Page.GoogleDefinition(text, sourceLang) }
    }

    fun onGoogleImageSearchClicked() {
        viewScope.launch { _loadUrl.value = Page.GoogleImageSearch(text, sourceLang) }
    }

    fun onGoogleSearchClicked() {
        viewScope.launch { _loadUrl.value = Page.GoogleSearch(text, sourceLang) }
    }

    fun onWikipediaClicked() {
        viewScope.launch { _loadUrl.value = Page.Wikipedia(text, sourceLang) }
    }

    sealed class Page(val text: String, val sourceLang: String) {
        companion object {
            fun default(text: String, sourceLang: String): Page {
                return GoogleTranslate(text, sourceLang)
            }
        }

        abstract val url: String
        val encodedText: String get() = URLEncoder.encode(text, "utf-8")

        class GoogleTranslate(text: String, sourceLang: String) : Page(text, sourceLang) {
            override val url: String
                get() = "https://translate.google.com/?sl=$sourceLang&text=$encodedText&op=translate"
        }

        class Wikipedia(text: String, sourceLang: String) : Page(text, sourceLang) {
            override val url: String
                get() = "https://www.wikipedia.org/w/index.php?search=$encodedText"
        }

        class GoogleSearch(text: String, sourceLang: String) : Page(text, sourceLang) {
            override val url: String
                get() = "https://www.google.com/search?q=$encodedText"
        }

        class GoogleDefinition(text: String, sourceLang: String) : Page(text, sourceLang) {
            override val url: String
                get() = "https://www.google.com/search?q=define:$encodedText"
        }

        class GoogleImageSearch(text: String, sourceLang: String) : Page(text, sourceLang) {
            override val url: String
                get() = "https://www.google.com/search?tbm=isch&q=$encodedText"
        }
    }
}
