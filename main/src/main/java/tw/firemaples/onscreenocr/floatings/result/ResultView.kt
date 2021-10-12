package tw.firemaples.onscreenocr.floatings.result

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.floatings.manager.Result
import tw.firemaples.onscreenocr.recognition.RecognitionResult
import tw.firemaples.onscreenocr.translator.TranslationProviderType
import tw.firemaples.onscreenocr.utils.setTextOrGone
import tw.firemaples.onscreenocr.utils.showOrHide

class ResultView(context: Context) : FloatingView(context) {

    override val layoutId: Int
        get() = R.layout.floating_result_view

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    private val viewModel: ResultViewModel by lazy { ResultViewModel(viewScope) }

    var onUserDismiss: (() -> Unit)? = null

    init {
        setViews()
    }

    private fun setViews() {
        val viewRoot: View = rootView.findViewById(R.id.viewRoot)

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

        val viewTranslationBlock: View = rootView.findViewById(R.id.view_translationBlock)

        val tvTranslationProvider: TextView = rootView.findViewById(R.id.tv_translationProvider)
        val viewTranslatedByGoogle: View = rootView.findViewById(R.id.iv_translatedByGoogle)

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
            viewTranslationBlock.showOrHide(it)
        }

        viewModel.translationProviderText.observe(lifecycleOwner) {
            tvTranslationProvider.setTextOrGone(it)
        }
        viewModel.displayTranslatedByGoogle.observe(lifecycleOwner) {
            viewTranslatedByGoogle.showOrHide(it)
        }

        viewRoot.setOnClickListener { onUserDismiss?.invoke() }
    }

    fun startRecognition() {
        attachToScreen()
        viewModel.startRecognition()
    }

    fun textRecognized(result: RecognitionResult) {
        viewModel.textRecognized(result)
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
}
