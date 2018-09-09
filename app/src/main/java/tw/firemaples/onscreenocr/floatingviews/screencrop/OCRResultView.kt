package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RelativeLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.ocr.OcrResult
import tw.firemaples.onscreenocr.state.OCRProcessState
import tw.firemaples.onscreenocr.state.TranslatedState
import tw.firemaples.onscreenocr.state.TranslatingState
import tw.firemaples.onscreenocr.state.event.StateChangedEvent
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.removeFromParent
import tw.firemaples.onscreenocr.views.OCRResultWindow

class OCRResultView(context: Context) : FloatingView(context) {
    private val logger: Logger = LoggerFactory.getLogger(OCRResultView::class.java)

    private val viewResultWrapper: RelativeLayout by lazy {
        rootView.findViewById<RelativeLayout>(R.id.view_resultWrapper)
    }

    private val ocrResultWindow: OCRResultWindow by lazy { OCRResultWindow(context) }

    private val resultCovers = mutableListOf<ImageView>()
    private val ivDebugImage by lazy { ImageView(getContext()) }

    override fun getLayoutId(): Int = R.layout.view_result_view
    override fun layoutFocusable(): Boolean = true
    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT
    override fun fullScreenMode(): Boolean = true

    init {
        setViews()
    }

    private fun setViews() {
        rootView.setOnClickListener {
            onBackButtonPressed()
        }
    }

    override fun attachToWindow() {
        super.attachToWindow()
        EventUtil.register(this)
    }

    override fun detachFromWindow() {
        EventUtil.unregister(this)
        super.detachFromWindow()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onStateChanged(event: StateChangedEvent) {
        when (event.state) {
            OCRProcessState -> {
                clearResultCovers()
                removeResultWindow()
            }

            TranslatingState -> {
                val ocrResult = StateManager.ocrResultList.first()
                drawResultCovers(ocrResult)
                showResultWindow(ocrResult)
            }

            TranslatedState -> {

            }

            else -> {
                detachFromWindow()
            }
        }
    }

    private fun drawResultCovers(ocrResult: OcrResult) {
        clearResultCovers()

        val parentRect = ocrResult.rect
        ocrResult.boxRects.forEach { rect ->
            val ivCover = ImageView(context)
            val layoutParams = RelativeLayout.LayoutParams(rect.width(), rect.height())
            layoutParams.setMargins(parentRect.left + rect.left, parentRect.top + rect.top, 0, 0)
            ivCover.layoutParams = layoutParams
            ivCover.setBackgroundColor(
                    ResourcesCompat.getColor(context.resources,
                            R.color.ocrResultCover_color,
                            null))

            viewResultWrapper.addView(ivCover)
            resultCovers.add(ivCover)
        }
    }

    private fun clearResultCovers() {
        resultCovers.forEach { it.removeFromParent() }
        resultCovers.clear()
    }

    private fun showResultWindow(ocrResult: OcrResult) {
        val parentRect = ocrResult.rect
        val layoutParams = RelativeLayout.LayoutParams(parentRect.width(), parentRect.height())
        layoutParams.setMargins(parentRect.left, parentRect.top, 0, 0)
        ivDebugImage.layoutParams = layoutParams

        val bitmap = ocrResult.debugInfo?.croppedBitmap
        if (SettingUtil.isDebugMode && bitmap != null) {
            ivDebugImage.setImageBitmap(bitmap)
        } else {
            ivDebugImage.setImageBitmap(null)
        }

        if (ivDebugImage.parent == null) {
            viewResultWrapper.addView(ivDebugImage, 0)
        }

        if (ocrResultWindow.parent == null) {
            viewResultWrapper.addView(ocrResultWindow)
            ocrResultWindow.anchor(ivDebugImage)
        }
    }

    private fun removeResultWindow() {
        ocrResultWindow.removeFromParent()
        ivDebugImage.removeFromParent()
    }
}