package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.Context
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.runtime.Composable
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeFloatingView
import tw.firemaples.onscreenocr.floatings.compose.base.collectOnLifecycleResumed
import tw.firemaples.onscreenocr.floatings.recognizedTextEditor.RecognizedTextEditor
import tw.firemaples.onscreenocr.floatings.result.FontSizeAdjuster
import tw.firemaples.onscreenocr.floatings.textInfoSearch.TextInfoSearchView
import tw.firemaples.onscreenocr.translator.utils.GoogleTranslateUtils
import tw.firemaples.onscreenocr.utils.Logger
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.getViewRect
import javax.inject.Inject

class ResultViewFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: ResultViewModel,
) : ComposeFloatingView(context) {

    private val logger: Logger by lazy { Logger(ResultViewFloatingView::class) }

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val enableHomeButtonWatcher: Boolean
        get() = true

    @Composable
    override fun RootContent() {
        viewModel.action.collectOnLifecycleResumed { action ->
            when (action) {
                is ResultViewAction.LaunchGoogleTranslator -> {
                    GoogleTranslateUtils.launchTranslator(action.text)
                }

                is ResultViewAction.ShareText -> {
                    Utils.shareText(action.text)
                }

                ResultViewAction.ShowFontSizeAdjuster ->
                    FontSizeAdjuster(context).attachToScreen()

                is ResultViewAction.ShowOCRTextEditor ->
                    showRecognizedTextEditor(
                        text = action.text,
                        croppedBitmap = action.croppedBitmap,
                        onTextEdited = viewModel::onOCRTextEdited,
                    )

                is ResultViewAction.ShowTextInfoSearchView -> {
                    TextInfoSearchView(
                        context = context,
                        text = action.text,
                        sourceLang = action.sourceLang,
                        targetLang = action.targetLang,
                    ).attachToScreen()
                }
            }
        }

        ResultViewContent(
            viewModel = viewModel,
            requestRootLocationOnScreen = rootView::getViewRect,
        )
    }

    private fun showRecognizedTextEditor(
        text: String,
        croppedBitmap: Bitmap,
        onTextEdited: (String) -> Unit,
    ) {
        RecognizedTextEditor(
            context = context,
            review = croppedBitmap,
            text = text,
            onSubmit = {
                if (it.isNotBlank() && it.trim() != text) {
                    onTextEdited.invoke(it.trim())
                }
            },
        ).attachToScreen()
    }

    override fun onHomeButtonPressed() {
        super.onHomeButtonPressed()
        viewModel.onHomeButtonPressed()
    }
}
