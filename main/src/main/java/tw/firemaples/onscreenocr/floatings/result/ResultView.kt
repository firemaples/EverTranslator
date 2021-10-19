package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.graphics.Rect
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.dialog.DialogView
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.*

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

    private val viewRoot: RelativeLayout = rootView.findViewById(R.id.viewRoot)

    var onUserDismiss: (() -> Unit)? = null

    private val viewResultWindow: View = rootView.findViewById(R.id.view_resultWindow)

    private var unionRect: Rect = Rect()

    init {
        setViews()
    }

    private fun setViews() {
        val btReadOutOCRText: View = rootView.findViewById(R.id.bt_readOutOCRText)
        val btEditOCRText: View = rootView.findViewById(R.id.bt_editOCRText)
        val btCopyOCRText: View = rootView.findViewById(R.id.bt_copyOCRText)
        val btTranslateOCRTextWithGoogleTranslate: View =
            rootView.findViewById(R.id.bt_translateOCRTextWithGoogleTranslate)

        val btReadOutTranslatedText: View = rootView.findViewById(R.id.bt_readOutTranslatedText)
        val btCopyTranslatedText: View = rootView.findViewById(R.id.bt_copyTranslatedText)
        val btTranslateTranslatedTextWithGoogleTranslate: View =
            rootView.findViewById(R.id.bt_translateTranslatedTextWithGoogleTranslate)

        val pbOCROperating: View = rootView.findViewById(R.id.pb_ocrOperating)
        val pbTranslating: View = rootView.findViewById(R.id.pb_translationOperating)

        val tvOCRText: TextView = rootView.findViewById(R.id.tv_ocrText)
        val tvTranslatedText: TextView = rootView.findViewById(R.id.tv_translatedText)

        val groupTranslationViews: Group = rootView.findViewById(R.id.group_translationViews)

        val tvTranslationProvider: TextView = rootView.findViewById(R.id.tv_translationProvider)
        val viewTranslatedByGoogle: View = rootView.findViewById(R.id.iv_translatedByGoogle)

        val boundingBoxView: TextBoundingBoxView =
            rootView.findViewById(R.id.view_textBoundingBoxView)

        viewModel.displayOCROperationProgress.observe(lifecycleOwner) {
            pbOCROperating.showOrHide(it)
        }
        viewModel.displayTranslationProgress.observe(lifecycleOwner) {
            pbTranslating.showOrHide(it)
        }

        viewModel.ocrText.observe(lifecycleOwner) {
            tvOCRText.text = it
        }
        viewModel.translatedText.observe(lifecycleOwner) {
            tvTranslatedText.text = it
        }

        viewModel.displayTranslatedBlock.observe(lifecycleOwner) {
            groupTranslationViews.showOrHide(it)
        }

        viewModel.translationProviderText.observe(lifecycleOwner) {
            tvTranslationProvider.setTextOrGone(it)
        }
        viewModel.displayTranslatedByGoogle.observe(lifecycleOwner) {
            viewTranslatedByGoogle.showOrHide(it)
        }

        viewModel.displayRecognizedTextAreas.observe(lifecycleOwner) {
            val (boundingBoxes, unionRect) = it
            boundingBoxView.boundingBoxes = boundingBoxes
            updateSelectedAreas(unionRect)
        }

        tvOCRText.movementMethod = ScrollingMovementMethod()
        tvTranslatedText.movementMethod = ScrollingMovementMethod()
        viewRoot.setOnClickListener { onUserDismiss?.invoke() }
        btEditOCRText.setOnClickListener {
            showRecognizedTextEditor(tvOCRText.text.toString())
        }
        btCopyOCRText.setOnClickListener {
            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, tvOCRText.text.toString())
        }
        btCopyTranslatedText.setOnClickListener {
            Utils.copyToClipboard(LABEL_TRANSLATED_TEXT, tvTranslatedText.text.toString())
        }
        btTranslateOCRTextWithGoogleTranslate.setOnClickListener {
            GoogleTranslateUtils.launchGoogleTranslateApp(tvOCRText.text.toString())
            onUserDismiss?.invoke()
        }
        btTranslateTranslatedTextWithGoogleTranslate.setOnClickListener {
            GoogleTranslateUtils.launchGoogleTranslateApp(tvTranslatedText.text.toString())
            onUserDismiss?.invoke()
        }
    }

    private fun showRecognizedTextEditor(recognizedText: String) {
        DialogView(context, layoutFocusable = true).apply {
            val etOCRText = View.inflate(context, R.layout.view_edittext, null) as EditText
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
