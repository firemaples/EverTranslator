package tw.firemaples.onscreenocr.floatings.textInfoSearch

import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_OFF
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.FloatingTextInfoSearchBinding
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.clickOnce

class TextInfoSearchView(
    context: Context,
    private val text: String,
    private val sourceLang: String,
    private val targetLang: String,
) : FloatingView(context) {
    private val logger: Logger by lazy { Logger(this::class) }

    override val layoutId: Int
        get() = R.layout.floating_text_info_search

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutFocusable: Boolean
        get() = true

    override val enableHomeButtonWatcher: Boolean
        get() = true

    private val viewModel: TextInfoSearchViewModel by lazy {
        TextInfoSearchViewModel(viewScope, text, sourceLang)
    }

    private val binding: FloatingTextInfoSearchBinding =
        FloatingTextInfoSearchBinding.bind(rootLayout)

    init {
        binding.setViews()
        viewModel.onLoad()
    }

    private fun FloatingTextInfoSearchBinding.setViews() {
        viewModel.loadUrl.observe(lifecycleOwner) {
            logger.debug("Load web: ${it.url}")
            webView.loadUrl(it.url)
        }

        close.clickOnce { detachFromScreen() }
        prevPage.clickOnce { webView.goBack() }
        nextPage.clickOnce { webView.goForward() }
        refresh.clickOnce { viewModel.onRefreshClicked() }
        googleTranslate.clickOnce { viewModel.onGoogleTranslateClicked() }
        googleDefinition.clickOnce { viewModel.onGoogleDefinitionClicked() }
        googleImageSearch.clickOnce { viewModel.onGoogleImageSearchClicked() }
        googleSearch.clickOnce { viewModel.onGoogleSearchClicked() }
        wikipedia.clickOnce { viewModel.onWikipediaClicked() }

        webView.setupWebView(swipeProgress)
    }

    private fun WebView.setupWebView(swipeRefreshLayout: SwipeRefreshLayout) {
        WebView.setWebContentsDebuggingEnabled(true)

        webChromeClient = ProgressWebChromeClient(swipeRefreshLayout)
        val webViewClient = MyWebViewClient()
        this.webViewClient = webViewClient

        // http://www.coderzheaven.com/2016/12/23/important-steps-to-improve-android-webview-performance/
        with(settings) {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }
        scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        isScrollbarFadingEnabled = true

        viewTreeObserver.addOnScrollChangedListener {
            swipeRefreshLayout.isEnabled = scrollY == 0
        }

        swipeRefreshLayout.setOnRefreshListener {
            reload()
        }

        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    WebSettingsCompat.setForceDark(this.settings, FORCE_DARK_ON)
                }
                Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_UNDEFINED -> {
                    WebSettingsCompat.setForceDark(this.settings, FORCE_DARK_OFF)
                }
                else -> {
                    //
                }
            }
        }
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        detachFromScreen()
    }

    override fun onBackButtonPressed(): Boolean {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            detachFromScreen()
        }
        return true
    }

    private class MyWebViewClient : WebViewClient() {
        private var clearHistory = false

        fun clearHistory() {
            clearHistory = true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            if (clearHistory) {
                clearHistory = false
                view?.clearHistory()
            }

            super.onPageFinished(view, url)
        }
    }

    private class ProgressWebChromeClient(val swipeRefreshLayout: SwipeRefreshLayout) :
        WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            swipeRefreshLayout.isRefreshing = newProgress < 100
        }
    }
}
