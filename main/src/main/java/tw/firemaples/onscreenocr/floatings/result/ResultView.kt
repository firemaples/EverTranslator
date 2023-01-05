package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.graphics.Rect
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.FloatingResultViewBinding
import tw.firemaples.onscreenocr.databinding.ViewEdittextBinding
import tw.firemaples.onscreenocr.databinding.ViewResultPanelBinding
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ResultView(context: Context) : FloatingView(context) {
    companion object {
        private const val LABEL_RECOGNIZED_TEXT = "Recognized text"
        private const val LABEL_TRANSLATED_TEXT = "Translated text"
    }

    override val layoutId: Int
        get() = R.layout.floating_result_view

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val enableHomeButtonWatcher: Boolean
        get() = true

    private val viewModel: ResultViewModel by lazy { ResultViewModel(viewScope) }

    private val binding: FloatingResultViewBinding = FloatingResultViewBinding.bind(rootLayout)

    private val viewRoot: RelativeLayout = binding.viewRoot

    var onUserDismiss: (() -> Unit)? = null

    private val viewResultWindow: View = binding.viewResultWindow

    private var unionRect: Rect = Rect()

    init {
        binding.resultPanel.setViews()
    }

    private fun ViewResultPanelBinding.setViews() {
        viewModel.displayOCROperationProgress.observe(lifecycleOwner) {
            pbOcrOperating.showOrHide(it)
        }
        viewModel.displayTranslationProgress.observe(lifecycleOwner) {
            pbTranslationOperating.showOrHide(it)
        }

        viewModel.ocrText.observe(lifecycleOwner) {
            tvOcrText.text = it
            mywebview.loadUrl("about:blank")
        }
        viewModel.translatedText.observe(lifecycleOwner) {
            if (it == null) {
                tvTranslatedText.text = null
            } else {
                val (text, color) = it
                tvTranslatedText.text = text
                tvTranslatedText.setTextColor(ContextCompat.getColor(context, color))
            }

            reposition()
        }

        viewModel.displayRecognitionBlock.observe(lifecycleOwner) {
            groupRecognitionViews.showOrHide(it)
        }
        viewModel.displayTranslatedBlock.observe(lifecycleOwner) {
            groupTranslationViews.showOrHide(it)
        }

        viewModel.translationProviderText.observe(lifecycleOwner) {
            tvTranslationProvider.setTextOrGone(it)
        }
        viewModel.displayTranslatedByGoogle.observe(lifecycleOwner) {
            ivTranslatedByGoogle.showOrHide(it)
        }

        viewModel.displayRecognizedTextAreas.observe(lifecycleOwner) {
            val (boundingBoxes, unionRect) = it
            binding.viewTextBoundingBoxView.boundingBoxes = boundingBoxes
            updateSelectedAreas(unionRect)
        }

        viewModel.copyRecognizedText.observe(lifecycleOwner) {
            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, it)
        }

        viewModel.fontSize.observe(lifecycleOwner) {
            tvOcrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
            tvTranslatedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
        }

        tvOcrText.movementMethod = ScrollingMovementMethod()
        tvTranslatedText.movementMethod = ScrollingMovementMethod()
        viewRoot.clickOnce { onUserDismiss?.invoke() }
        btEditOCRText.clickOnce {
            showRecognizedTextEditor(tvOcrText.text.toString())
        }
        btCopyOCRText.clickOnce {
            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, tvOcrText.text.toString())
        }
        btCopyTranslatedText.clickOnce {
            Utils.copyToClipboard(LABEL_TRANSLATED_TEXT, tvTranslatedText.text.toString())
        }
        btTranslateOCRTextWithGoogleTranslate.clickOnce {
            GoogleTranslateUtils.launchGoogleTranslateApp(tvOcrText.text.toString())
            onUserDismiss?.invoke()
        }
        btTranslateTranslatedTextWithGoogleTranslate.clickOnce {
            GoogleTranslateUtils.launchGoogleTranslateApp(tvTranslatedText.text.toString())
            onUserDismiss?.invoke()
        }
        btShareOCRText.clickOnce {
            val ocrText = viewModel.ocrText.value ?: return@clickOnce
            Utils.shareText(ocrText)
            onUserDismiss?.invoke()
        }
        btAdjustFontSize.clickOnce {
            FontSizeAdjuster(context).attachToScreen()
        }
         btSelectwords.clickOnce {
            val p: Pattern = Pattern.compile("[^\\s]+")

            val m: Matcher = p.matcher(tvOcrText.text.toString())

            val ss = SpannableString(tvOcrText.text.toString())

            while (m.find()) {
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(textView: View) {
                        val s = tvOcrText.getText() as Spanned
                        val start = s.getSpanStart(this)
                        val end = s.getSpanEnd(this)

                        //test if the code work
                        Toast.makeText(context, s.subSequence(start, end),Toast.LENGTH_SHORT).show()

                        // set webview and hide some html elements 
                         mywebview.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                view.loadUrl(
                                    "javascript:(function() { " +
                                            "var head = document.getElementsByClassName('qlS7ne')[0].style.display='true'; " +
                                            "var head = document.getElementsByClassName('wQnou')[0].style.display='none'; " +
                                            "var head = document.getElementsByClassName('iSZmU')[0].style.display='none'; " +
                                            "var head = document.getElementsByClassName('FAZ4xe FoDaAb')[0].style.display='none'; " +
                                            "})()"
                                )                    }            }
                        mywebview.loadUrl("https://www.google.com/search?tbm=isch&q="+ s.subSequence(start, end) )

                        
                    }
                }
                ss.setSpan(clickableSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                tvOcrText.text = ss
                tvOcrText.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    private fun showRecognizedTextEditor(recognizedText: String) {
        object : DialogView(context) {
            override val layoutFocusable: Boolean
                get() = true
        }.apply {
            val contentBinding = ViewEdittextBinding.inflate(LayoutInflater.from(context))
            val etOCRText = contentBinding.root

            etOCRText.setText(recognizedText)
            setContentView(etOCRText)

            onButtonOkClicked = {
                val text = etOCRText.text.toString()
                if (text.isNotBlank() && text.trim() != recognizedText) {
                    viewModel.onOCRTextEdited(text)
                }
            }

            attachToScreen()
        }
    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
        viewResultWindow.visibility = View.INVISIBLE
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        onUserDismiss?.invoke()
    }

    fun startRecognition() {
        attachToScreen()
        viewModel.startRecognition()
    }

    fun textRecognized(result: RecognitionResult, parent: Rect, selected: Rect) {
        viewModel.textRecognized(result, parent, selected, rootView.getViewRect())
    }

    fun startTranslation(translationProviderType: TranslationProviderType) {
        viewModel.startTranslation(translationProviderType)
    }

    fun textTranslated(result: Result) {
        viewModel.textTranslated(result)
    }

    fun backToIdle() {
        detachFromScreen()
    }

    private fun updateSelectedAreas(unionRect: Rect) {
        this.unionRect = unionRect
        reposition()
    }

    private fun reposition() {
        rootView.post {
            val parentRect = viewRoot.getViewRect()
            val anchorRect = unionRect.apply {
                top += parentRect.top
                left += parentRect.left
                bottom += parentRect.top
                right += parentRect.left
            }
            val windowRect = viewResultWindow.getViewRect()

            val (leftMargin, topMargin) = UIUtils.countViewPosition(
                anchorRect, parentRect,
                windowRect.width(), windowRect.height(), 2.dpToPx(),
            )

            val layoutParams =
                (viewResultWindow.layoutParams as RelativeLayout.LayoutParams).apply {
                    this.leftMargin = leftMargin
                    this.topMargin = topMargin
                }

            viewRoot.updateViewLayout(viewResultWindow, layoutParams)

            viewRoot.post {
                viewResultWindow.visibility = View.VISIBLE
            }
        }
    }
}
