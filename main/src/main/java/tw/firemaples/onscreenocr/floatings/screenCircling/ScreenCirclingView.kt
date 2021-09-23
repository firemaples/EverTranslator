package tw.firemaples.onscreenocr.floatings.screenCircling

import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.base.FloatingView

class ScreenCirclingView(context: Context) : FloatingView(context) {
    override val layoutId: Int
        get() = R.layout.floating_screen_circling

    //TODO check this
    override val fullscreenMode: Boolean
        get() = true

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT
    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    private val circlingView: CirclingView = rootView.findViewById(R.id.view_circlingView)
    private val progressBorderView: ProgressBorderView =
        rootView.findViewById(R.id.view_progressBorder)
    private val helperTextView: HelperTextView = rootView.findViewById(R.id.view_helperText)

    private val viewModel: ScreenCirclingViewModel by lazy { ScreenCirclingViewModel(viewScope) }

    var onAreaSelected: ((Rect) -> Unit)? = null

    init {
        setViews()
    }

    private fun setViews() {
        circlingView.helperTextView = helperTextView
        circlingView.onAreaSelected = {
            onAreaSelected?.invoke(it)
        }

        viewModel.lastSelectedArea.observe(lifecycleOwner) {
            circlingView.selectedBox = it
        }

        viewModel.load()
    }

    override fun attachToScreen() {
        super.attachToScreen()

        progressBorderView.start()
    }

    override fun detachFromScreen() {
        super.detachFromScreen()

        circlingView.clear()
        progressBorderView.stop()
    }
}
