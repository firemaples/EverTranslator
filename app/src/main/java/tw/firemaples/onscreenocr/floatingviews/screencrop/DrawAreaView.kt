package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import android.view.WindowManager
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.views.AreaSelectionView
import tw.firemaples.onscreenocr.views.FadeOutHelpTextView
import tw.firemaples.onscreenocr.views.OnAreaSelectionViewCallback
import tw.firemaples.onscreenocr.views.ProgressBorderView

/**
 * Created by firemaples on 21/10/2016.
 */

class DrawAreaView(context: Context) : FloatingView(context) {
    val areaSelectionView: AreaSelectionView = rootView.findViewById(R.id.view_areaSelectionView)
    private val progressBorderView: ProgressBorderView = rootView
            .findViewById(R.id.view_progressBorder)
    private val helpTextView: FadeOutHelpTextView = rootView.findViewById(R.id.view_helpText)

    init {
        areaSelectionView.helpTextView = helpTextView
    }

    override fun getLayoutId(): Int = R.layout.view_draw_area

    override fun fullScreenMode(): Boolean = true

    override fun layoutFocusable(): Boolean = true

    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT

    override fun attachToWindow() {
        super.attachToWindow()
        progressBorderView.start()

        if (SettingUtil.isRememberLastSelection) {
            areaSelectionView.setBoxList(SettingUtil.lastSelectionArea)
        }
    }

    override fun detachFromWindow() {
        areaSelectionView.clear()
        progressBorderView.stop()
        super.detachFromWindow()
    }

    fun setCallback(callback: OnAreaSelectionViewCallback) {
        areaSelectionView.callback = callback
    }
}
