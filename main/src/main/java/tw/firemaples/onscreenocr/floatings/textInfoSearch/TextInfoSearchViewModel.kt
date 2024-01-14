package tw.firemaples.onscreenocr.floatings.textInfoSearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.floatings.base.FloatingViewModel
import tw.firemaples.onscreenocr.pref.AppPref
import tw.firemaples.onscreenocr.utils.Constants
import java.net.URLEncoder

class TextInfoSearchViewModel(
    viewScope: CoroutineScope,
    private val text: String,
    private val sourceLang: String,
    private val targetLang: String,
) : FloatingViewModel(viewScope) {
    private val _loadUrl = MutableLiveData<Page>()
    val loadUrl: LiveData<Page> = _loadUrl

    private var lastPageType: PageType
        get() = PageType.entries.firstOrNull { it.id == AppPref.lastTextInfoSearchPage }
            ?: Constants.DEFAULT_TEXT_INFO_SEARCH_PAGE
        set(value) {
            AppPref.lastTextInfoSearchPage = value.id
        }

    fun onLoad() {
        viewScope.launch {
            val page: Page = when (lastPageType) {
                PageType.GoogleTranslate -> Page.GoogleTranslate(text, sourceLang, targetLang)
                PageType.GoogleSearch -> Page.GoogleSearch(text, sourceLang)
                PageType.GoogleDefinition -> Page.GoogleDefinition(text, sourceLang)
                PageType.GoogleImageSearch -> Page.GoogleImageSearch(text, sourceLang)
                PageType.Wikipedia -> Page.Wikipedia(text, sourceLang)
            }
            loadPage(page)
        }
    }

    fun onRefreshClicked() {
        viewScope.launch {
            _loadUrl.value = _loadUrl.value
        }
    }

    fun onGoogleTranslateClicked() {
        viewScope.launch {
            loadPage(Page.GoogleTranslate(text, sourceLang, targetLang))
        }
    }

    fun onGoogleDefinitionClicked() {
        viewScope.launch {
            loadPage(Page.GoogleDefinition(text, sourceLang))
        }
    }

    fun onGoogleImageSearchClicked() {
        viewScope.launch {
            loadPage(Page.GoogleImageSearch(text, sourceLang))
        }
    }

    fun onGoogleSearchClicked() {
        viewScope.launch {
            loadPage(Page.GoogleSearch(text, sourceLang))
        }
    }

    fun onWikipediaClicked() {
        viewScope.launch {
            loadPage(Page.Wikipedia(text, sourceLang))
        }
    }

    private fun loadPage(page: Page) {
        lastPageType = page.pageType
        _loadUrl.value = page
    }

    enum class PageType(val id: Int) {
        GoogleTranslate(1),
        GoogleSearch(2),
        GoogleDefinition(3),
        GoogleImageSearch(4),
        Wikipedia(5),
    }

    sealed class Page(val text: String, val sourceLang: String, val pageType: PageType) {
        companion object {
            fun default(text: String, sourceLang: String, targetLang: String): Page {
                return GoogleTranslate(text, sourceLang, targetLang)
            }
        }

        abstract val url: String
        val encodedText: String get() = URLEncoder.encode(text, "utf-8")

        class GoogleTranslate(text: String, sourceLang: String, val targetLang: String) :
            Page(text, sourceLang, PageType.GoogleTranslate) {
            override val url: String
                get() = "https://translate.google.com/?sl=$sourceLang&tl=$targetLang&text=$encodedText&op=translate"
        }

        class Wikipedia(text: String, sourceLang: String) :
            Page(text, sourceLang, PageType.Wikipedia) {
            override val url: String
                get() = "https://www.wikipedia.org/w/index.php?search=$encodedText"
        }

        class GoogleSearch(text: String, sourceLang: String) :
            Page(text, sourceLang, PageType.GoogleSearch) {
            override val url: String
                get() = "https://www.google.com/search?q=$encodedText"
        }

        class GoogleDefinition(text: String, sourceLang: String) :
            Page(text, sourceLang, PageType.GoogleDefinition) {
            override val url: String
                get() = "https://www.google.com/search?q=define:$encodedText"
        }

        class GoogleImageSearch(text: String, sourceLang: String) :
            Page(text, sourceLang, PageType.GoogleImageSearch) {
            override val url: String
                get() = "https://www.google.com/search?tbm=isch&q=$encodedText"
        }
    }
}
