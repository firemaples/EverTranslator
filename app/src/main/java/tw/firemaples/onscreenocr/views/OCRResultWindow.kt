package tw.firemaples.onscreenocr.views

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatings.screencrop.TTSPlayerView
import tw.firemaples.onscreenocr.floatings.screencrop.TextEditDialogView
import tw.firemaples.onscreenocr.ocr.tesseract.OCRLangUtil
import tw.firemaples.onscreenocr.state.TranslatedState
import tw.firemaples.onscreenocr.state.TranslatingState
import tw.firemaples.onscreenocr.state.event.StateChangedEvent
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.utils.*

class OCRResultWindow(context: Context) : FrameLayout(context) {
    private val logger = LoggerFactory.getLogger(OCRResultWindow::class.java)

    private val MARGIN_PX = 4f
    private val layoutMargin by lazy { UIUtil.dpToPx(context, MARGIN_PX) }

    private val view: View by lazy { View.inflate(context, R.layout.view_ocr_result_window, null) }

    private val pbOri by lazy { view.getView(R.id.pb_origin) }
    private val tvOriText by lazy { view.getTextView(R.id.tv_originText) }
    private val btCopyOT by lazy { view.getView(R.id.bt_copy_ocrText) }
    private val btEditOT by lazy { view.getView(R.id.bt_edit_ocrText) }
    private val btTtsOT by lazy { view.getView(R.id.bt_tts_ocrText) }
    private val btOpenGtOT by lazy { view.getView(R.id.bt_openGoogleTranslate_ocrText) }

    private val wrapperTranslatedArea by lazy { view.getView(R.id.view_translatedTextWrapper) }
    private val pbTranslated by lazy { view.getView(R.id.pb_translated) }
    private val tvTranslated by lazy { view.getTextView(R.id.tv_translatedText) }
    private val btCopyTT by lazy { view.getView(R.id.bt_copy_translatedText) }
    private val btTtsTT by lazy { view.getView(R.id.bt_tts_translatedText) }
    private val btOpenGtTT by lazy { view.getView(R.id.bt_openGoogleTranslate_translatedText) }

    private val tvTranslationService by lazy { view.getTextView(R.id.tv_translationService) }
    private val ivTranslationService by lazy { view.findViewById<ImageView>(R.id.iv_translationService) }

    private val layoutParams by lazy {
        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            setMargins(layoutMargin, layoutMargin, layoutMargin, layoutMargin)
        }
    }

    private val displayMetrics by lazy {
        val metrics = DisplayMetrics()
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
                .defaultDisplay.getMetrics(metrics)
        metrics
    }

    private val onClickListener: OnClickListener = OnClickListener { v ->
        when (v) {
            btTtsOT -> {
                showTTSPlayer(OCRLangUtil.selectLangDisplayCode,
                        tvOriText.text.toString())
            }
            btTtsTT -> {
                showTTSPlayer(TranslationUtil.currentTranslationLangCode,
                        tvTranslated.text.toString())
            }
            btCopyOT -> {
                Utils.copyToClipboard(Utils.LABEL_OCR_RESULT, tvOriText.text.toString())
            }
            btCopyTT -> {
                Utils.copyToClipboard(Utils.LABEL_TRANSLATED_TEXT, tvTranslated.text.toString())
            }
            btEditOT -> {
                changeOCRText()
            }
            btOpenGtOT -> {
                openGoogleTranslateApp(TranslationUtil.currentTranslationLangCode,
                        tvOriText.text.toString())
            }
            btOpenGtTT -> {
                openGoogleTranslateApp(OCRLangUtil.selectLangDisplayCode,
                        tvTranslated.text.toString())
            }
        }
    }

    init {
        isClickable = true
        isFocusable = true

        addView(view)
        btCopyOT.setOnClickListener(onClickListener)
        btEditOT.setOnClickListener(onClickListener)
        btTtsOT.setOnClickListener(onClickListener)
        btOpenGtOT.setOnClickListener(onClickListener)
        btCopyTT.setOnClickListener(onClickListener)
        btTtsTT.setOnClickListener(onClickListener)
        btOpenGtTT.setOnClickListener(onClickListener)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        EventUtil.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        EventUtil.unregister(this)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onStateChanged(event: StateChangedEvent) {
        wrapperTranslatedArea.setVisible(TranslationUtil.isEnableTranslation)

        val ocrFinished = event.state in arrayOf(TranslatingState, TranslatedState)
        val translated = event.state == TranslatedState

        pbOri.visibility = if (ocrFinished) View.GONE else View.VISIBLE
        pbTranslated.visibility = if (translated) View.GONE else View.VISIBLE

        var oriText: String? = null
        var translatedText: String? = null
        if (StateManager.ocrResultList.isNotEmpty()) {
            val ocrResult = StateManager.ocrResultList[0]
            oriText = if (ocrFinished) ocrResult.text else null
            translatedText = if (translated) ocrResult.translatedText else null
        }
        tvOriText.text = oriText
        tvTranslated.text = translatedText

        btCopyOT.isEnabled = ocrFinished
        btEditOT.isEnabled = ocrFinished
        btTtsOT.isEnabled = ocrFinished
        btOpenGtOT.isEnabled = ocrFinished

        btCopyTT.isEnabled = translated
        btTtsTT.isEnabled = translated
        btOpenGtTT.isEnabled = translated

        val service = TranslationUtil.currentService
        if (service.resultDrawableResId != -1) {
            ivTranslationService.setImageResource(service.resultDrawableResId)
            ivTranslationService.setVisible(true)
            tvTranslationService.setVisible(false)
        } else {
            val translatedBy = context.getString(
                    R.string.translatedBy, service.fullName)
            tvTranslationService.text = translatedBy
            tvTranslationService.setVisible(TranslationUtil.isEnableTranslation)
            ivTranslationService.setVisible(false)
        }
    }

    private var anchorView: View? = null
    fun anchor(anchorView: View) {
        this.anchorView = anchorView

        view.onViewPrepared {
            logger.info("onViewPrepared, width: ${it.width}, height: ${it.height}")
            adjustViewPosition()
        }
    }

    private fun adjustViewPosition1() {
        val anchorView = this.anchorView ?: return
        val parent = this.parent as ViewGroup

        val width = this.width
        val height = this.height

        val parentHeight = parent.height

        val needHeight = height + layoutMargin * 2

        if (anchorView.top > needHeight) {
            //Gravity = TOP
            layoutParams.topMargin = anchorView.top - height
        } else if (parentHeight - anchorView.bottom > needHeight) {
            //Gravity = BOTTOM
            layoutParams.topMargin = anchorView.top + anchorView.height
        } else {
            layoutParams.topMargin = layoutMargin
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        }

        if (anchorView.left + width + layoutMargin > displayMetrics.widthPixels) {
            // Match screen right
            layoutParams.leftMargin = displayMetrics.widthPixels - (width - layoutMargin)
        } else {
            // Match anchorView left
            layoutParams.leftMargin = anchorView.left
        }

        parent.updateViewLayout(this, layoutParams)
    }

    private fun adjustViewPosition() {
        val anchorView = this.anchorView ?: return
        val parent = this.parent as ViewGroup

        val width = this.width
        val height = this.height

        val parentHeight = parent.height

        val rect = Rect()
        anchorView.getGlobalVisibleRect(rect)

        val margins = UIUtil.countViewPosition(rect, width, height, layoutMargin, parentHeight)

        layoutParams.leftMargin = margins[0]
        layoutParams.topMargin = margins[1]

        if (layoutParams.topMargin == -1) {
            layoutParams.topMargin = layoutMargin
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL)
        }

        parent.updateViewLayout(this, layoutParams)
    }

    private fun showTTSPlayer(lang: String, text: String) {
        TTSPlayerView(context).apply {
            setTTSContent(lang, text)
            attachToWindow()
        }
    }

    private fun changeOCRText() {
        TextEditDialogView(context).apply {
            setTitle(context.getString(R.string.title_editOCRText))
            setContentText(tvOriText.text.toString())
            setCallback(object : TextEditDialogView.OnTextEditDialogViewCallback() {
                override fun onConfirmClick(textEditDialogView: TextEditDialogView?, text: String?) {
                    super.onConfirmClick(textEditDialogView, text)
                    if (text != null) {
                        StateManager.changeOCRText(text)
                    }
                }
            })

            attachToWindow()
        }
    }

    private fun openGoogleTranslateApp(langTo: String, text: String) {
        GoogleTranslateUtil.start(context, langTo, text)
        StateManager.onBackButtonPressed()
    }
}