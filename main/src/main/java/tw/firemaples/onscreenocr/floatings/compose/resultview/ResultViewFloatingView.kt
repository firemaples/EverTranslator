package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.view.WindowManager
import androidx.compose.runtime.Composable
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeFloatingView
import tw.firemaples.onscreenocr.floatings.compose.base.collectOnLifecycleResumed
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.setReusable
import javax.inject.Inject

class ResultViewFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: ResultViewModel,
) : ComposeFloatingView(context) {
    companion object {
        private const val LABEL_RECOGNIZED_TEXT = "Recognized text"
        private const val LABEL_TRANSLATED_TEXT = "Translated text"
    }

    private val logger: Logger by lazy { Logger(ResultViewFloatingView::class) }

//    override val layoutId: Int
//        get() = R.layout.floating_result_view

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val enableHomeButtonWatcher: Boolean
        get() = true

//    private val binding: FloatingResultViewBinding = FloatingResultViewBinding.bind(rootLayout)

//    private val viewRoot: RelativeLayout = binding.viewRoot

    var onUserDismiss: (() -> Unit)? = null

//    private val viewResultWindow: View = binding.viewResultWindow

    private var unionRect: Rect = Rect()

    private var croppedBitmap: Bitmap? = null

    @Composable
    override fun RootContent() {
        viewModel.action.collectOnLifecycleResumed { action ->
            when (action) {
                ResultViewAction.Close ->{
                    detachFromScreen()
                }

            }
        }

        ResultViewContent(viewModel)
    }

    init {
//        binding.resultPanel.setViews()
    }

//    private fun ViewResultPanelBinding.setViews() {
//        viewModel.displayOCROperationProgress.observe(lifecycleOwner) {
//            pbOcrOperating.showOrHide(it)
//        }
//        viewModel.displayTranslationProgress.observe(lifecycleOwner) {
//            pbTranslationOperating.showOrHide(it)
//        }
//        viewModel.displaySelectableText.observe(lifecycleOwner) {
//            textSelectable.isChecked = it
//            tvOcrText.showOrHide(!it)
//            tvWordBreakOcrText.showOrHide(it)
//        }
//        viewModel.ocrText.observe(lifecycleOwner) {
//            tvWordBreakOcrText.setContent(it?.text(), it?.locale() ?: Locale.getDefault())
//            tvOcrText.text = it?.text()
//        }
//        viewModel.translatedText.observe(lifecycleOwner) {
//            if (it == null) {
//                tvTranslatedText.text = null
//            } else {
//                val (text, color) = it
//                tvTranslatedText.text = text
//                tvTranslatedText.setTextColor(ContextCompat.getColor(context, color))
//            }
//
//            reposition()
//        }
//
//        viewModel.displayRecognitionBlock.observe(lifecycleOwner) {
//            groupRecognitionViews.showOrHide(it)
//        }
//        viewModel.displayTranslatedBlock.observe(lifecycleOwner) {
//            groupTranslationViews.showOrHide(it)
//        }
//
//        viewModel.translationProviderText.observe(lifecycleOwner) {
//            tvTranslationProvider.setTextOrGone(it)
//        }
//        viewModel.displayTranslatedByGoogle.observe(lifecycleOwner) {
//            ivTranslatedByGoogle.showOrHide(it)
//        }
//
//        viewModel.displayRecognizedTextAreas.observe(lifecycleOwner) {
//            val (boundingBoxes, unionRect) = it
//            binding.viewTextBoundingBoxView.boundingBoxes = boundingBoxes
//            updateSelectedAreas(unionRect)
//        }
//
//        viewModel.copyRecognizedText.observe(lifecycleOwner) {
//            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, it)
//        }
//
//        viewModel.fontSize.observe(lifecycleOwner) {
//            tvOcrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
//            tvWordBreakOcrText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
//            tvTranslatedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, it)
//        }
//
//        viewModel.displayTextInfoSearchView.observe(lifecycleOwner) {
//            TextInfoSearchView(context, it.text, it.sourceLang, it.targetLang)
//                .attachToScreen()
//        }
//
//        textSelectable.setOnCheckedChangeListener { _, checked ->
//            viewModel.onTextSelectableChecked(checked)
//        }
//        tvWordBreakOcrText.onWordClicked = { word ->
//            if (word != null) {
//                viewModel.onWordSelected(word)
//                tvWordBreakOcrText.clearSelection()
//            }
//        }
//        tvOcrText.movementMethod = ScrollingMovementMethod()
//        tvTranslatedText.movementMethod = ScrollingMovementMethod()
//        viewRoot.clickOnce { onUserDismiss?.invoke() }
//        btEditOCRText.clickOnce {
//            showRecognizedTextEditor(viewModel.ocrText.value?.text() ?: "")
//        }
//        btCopyOCRText.clickOnce {
//            Utils.copyToClipboard(LABEL_RECOGNIZED_TEXT, viewModel.ocrText.value?.text() ?: "")
//        }
//        btCopyTranslatedText.clickOnce {
//            Utils.copyToClipboard(LABEL_TRANSLATED_TEXT, tvTranslatedText.text.toString())
//        }
//        btTranslateOCRTextWithGoogleTranslate.clickOnce {
//            GoogleTranslateUtils.launchTranslator(viewModel.ocrText.value?.text() ?: "")
//            onUserDismiss?.invoke()
//        }
//        btTranslateTranslatedTextWithGoogleTranslate.clickOnce {
//            GoogleTranslateUtils.launchTranslator(tvTranslatedText.text.toString())
//            onUserDismiss?.invoke()
//        }
//        btShareOCRText.clickOnce {
//            val ocrText = viewModel.ocrText.value?.text() ?: return@clickOnce
//            Utils.shareText(ocrText)
//            onUserDismiss?.invoke()
//        }
//        btAdjustFontSize.clickOnce {
//            FontSizeAdjuster(context).attachToScreen()
//        }
//    }

//    private fun showRecognizedTextEditor(recognizedText: String) {
//        RecognizedTextEditor(
//            context = context,
//            review = croppedBitmap,
//            text = recognizedText,
//            onSubmit = {
//                if (it.isNotBlank() && it.trim() != recognizedText) {
//                    viewModel.onOCRTextEdited(it.trim())
//                }
//            },
//        ).attachToScreen()
//    }

    override fun onAttachedToScreen() {
        super.onAttachedToScreen()
//        viewResultWindow.visibility = View.INVISIBLE
    }

    override fun onDetachedFromScreen() {
        super.onDetachedFromScreen()
        this.croppedBitmap?.setReusable()
        this.croppedBitmap = null
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        onUserDismiss?.invoke()
    }

    fun startRecognition() {
        attachToScreen()
//        viewModel.startRecognition()
    }

    fun textRecognized(
        result: RecognitionResult,
        parent: Rect,
        selected: Rect,
        croppedBitmap: Bitmap
    ) {
        this.croppedBitmap = croppedBitmap
//        viewModel.textRecognized(result, parent, selected, rootView.getViewRect())
    }

    fun startTranslation(translationProviderType: TranslationProviderType) {
//        viewModel.startTranslation(translationProviderType)
    }

    fun textTranslated(result: Result) {
//        viewModel.textTranslated(result)
    }

    fun backToIdle() {
        detachFromScreen()
    }

    private fun updateSelectedAreas(unionRect: Rect) {
        this.unionRect = unionRect
        reposition()
    }

    private fun reposition() {
//        rootView.post {
//            val parentRect = viewRoot.getViewRect()
//            val anchorRect = Rect(unionRect).apply {
//                top += parentRect.top
//                left += parentRect.left
//                bottom += parentRect.top
//                right += parentRect.left
//            }
//            val windowRect = viewResultWindow.getViewRect()
//
//            val (leftMargin, topMargin) = UIUtils.countViewPosition(
//                anchorRect, parentRect,
//                windowRect.width(), windowRect.height(), 2.dpToPx(),
//            )
//
//            val layoutParams =
//                (viewResultWindow.layoutParams as RelativeLayout.LayoutParams).apply {
//                    this.leftMargin = leftMargin
//                    this.topMargin = topMargin
//                }
//
//            viewRoot.updateViewLayout(viewResultWindow, layoutParams)
//
//            viewRoot.post {
//                viewResultWindow.visibility = View.VISIBLE
//            }
//        }
    }
}
