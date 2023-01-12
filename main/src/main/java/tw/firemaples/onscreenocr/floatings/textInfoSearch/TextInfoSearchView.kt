package tw.firemaples.onscreenocr.floatings.textInfoSearch

import android.content.Context
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

    private val viewModel: TextInfoSearchViewModel by lazy { TextInfoSearchViewModel(viewScope) }

    private val binding: FloatingTextInfoSearchBinding =
        FloatingTextInfoSearchBinding.bind(rootLayout)

    init {
        setViews()
    }

    private fun setViews() {
        binding.viewRoot.clickOnce { detachFromScreen() }
        binding.webView.apply {
            setupWebView(this, binding.swipeProgress)
            val url =
                "https://translate.google.com/?sl=$sourceLang&tl=$targetLang&text=$text&op=translate"
            logger.debug("Load web: $url")
            loadUrl(url)
        }
    }

    private fun setupWebView(webView: WebView, swipeRefreshLayout: SwipeRefreshLayout) {
        WebView.setWebContentsDebuggingEnabled(true)

        webView.webChromeClient = ProgressWebChromeClient(swipeRefreshLayout)
        val webViewClient = MyWebViewClient()
        webView.webViewClient = webViewClient

        // http://www.coderzheaven.com/2016/12/23/important-steps-to-improve-android-webview-performance/
        with(webView.settings) {
            javaScriptEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(true)
            builtInZoomControls = false
            layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
            cacheMode = WebSettings.LOAD_NO_CACHE
            domStorageEnabled = true
        }
        webView.scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
        webView.isScrollbarFadingEnabled = true

        webView.viewTreeObserver.addOnScrollChangedListener {
            swipeRefreshLayout.isEnabled = webView.scrollY == 0
        }

        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        detachFromScreen()
    }

    override fun onBackButtonPressed(): Boolean {
        detachFromScreen()
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
