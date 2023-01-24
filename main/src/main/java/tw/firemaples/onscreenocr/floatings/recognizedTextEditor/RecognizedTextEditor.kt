package tw.firemaples.onscreenocr.floatings.recognizedTextEditor

import android.content.Context
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.databinding.ViewRecognizedTextEditorBinding
import tw.firemaples.onscreenocr.floatings.base.FloatingView
import tw.firemaples.onscreenocr.utils.clickOnce
import tw.firemaples.onscreenocr.utils.hideKeyboard
import tw.firemaples.onscreenocr.utils.showKeyboard

class RecognizedTextEditor(
    context: Context,
    review: Bitmap?,
    text: String,
    private val onSubmit: (String) -> Unit,
) :
    FloatingView(context) {
    override val layoutId: Int
        get() = R.layout.view_recognized_text_editor

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutFocusable: Boolean
        get() = true

    private val binding: ViewRecognizedTextEditorBinding =
        ViewRecognizedTextEditorBinding.bind(rootLayout)

    private val viewModel: RecognizedTextEditorViewModel by lazy {
        RecognizedTextEditorViewModel(viewScope)
    }

    init {
        binding.setViews()
        viewModel.init(text, review)
    }

    private fun ViewRecognizedTextEditorBinding.setViews() {
        viewModel.viewMaxHeight.observe(lifecycleOwner) {
            with(ConstraintSet()) {
                clone(viewRoot)
                constrainMaxHeight(R.id.review, it)
                applyTo(viewRoot)
            }
        }

        viewModel.text.observe(lifecycleOwner) {
            editor.setText(it)
        }

        viewModel.review.observe(lifecycleOwner) {
            review.setImageBitmap(it)
        }

        this.viewRoot.clickOnce {
            detachFromScreen()
        }
        this.cancel.clickOnce {
            detachFromScreen()
        }
        this.ok.clickOnce {
            onSubmit.invoke(editor.text.toString())
            detachFromScreen()
        }

        editor.requestFocus()
        viewScope.launch {
            delay(1L)
            editor.showKeyboard()
        }
    }

    override fun onBackButtonPressed(): Boolean {
        detachFromScreen()
        return true
    }

    override fun detachFromScreen() {
        rootView.hideKeyboard()
        super.detachFromScreen()
    }
}
