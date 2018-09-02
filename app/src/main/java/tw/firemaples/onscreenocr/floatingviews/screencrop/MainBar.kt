package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.*
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.floatingviews.MovableFloatingView
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.ocr.OCRManager
import tw.firemaples.onscreenocr.ocr.event.OCRLangChangedEvent
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.translate.event.TranslationLangChangedEvent
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.utils.Utils
import tw.firemaples.onscreenocr.utils.equalsAny
import tw.firemaples.onscreenocr.utils.setVisible
import tw.firemaples.onscreenocr.views.FloatingBarMenu
import java.util.*

class MainBar(context: Context) : MovableFloatingView(context), RealButtonHandler {
    private val logger: Logger = LoggerFactory.getLogger(MainBar::class.java)

    val viewLangSelector: View = rootView.findViewById(R.id.view_langSelector)
    val tvLang: TextView = rootView.findViewById(R.id.tv_lang)
    val ivGoogleTranslate: View = rootView.findViewById(R.id.iv_googleTranslate)
    val btSelectArea: View = rootView.findViewById(R.id.bt_selectArea)
    val btTranslation: View = rootView.findViewById(R.id.bt_translation)
    val btClear: View = rootView.findViewById(R.id.bt_clear)
    val pgProgress: View = rootView.findViewById(R.id.pg_progress)
    val viewMenu: View = rootView.findViewById(R.id.view_menu)

    var drawAreaView: DrawAreaView? = null
    var ocrResultView: OcrResultView? = null
    val dialogView: DialogView by lazy { DialogView(context) }

    init {
        setViews()
        setDragView(viewMenu)

        if (SettingUtil.startingWithSelectionMode) {
            StateManager.startSelection()
        }
    }

    override fun isPrimaryView(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.view_floating_bar_new

    override fun attachToWindow() {
        super.attachToWindow()
        SettingUtil.isAppShowing = true

        if (!SettingUtil.isVersionHistoryAlreadyShown) {
            VersionHistoryView(context).attachToWindow()
        }

        EventUtil.register(this)
    }

    fun detachFromWindow(reset: Boolean) {
        SettingUtil.isAppShowing = false
        if (reset) {
            detachFromWindow()
        } else {
            EventUtil.unregister(this)
            super.detachFromWindow()
        }
    }

    override fun detachFromWindow() {
        EventUtil.unregister(this)
        SettingUtil.isAppShowing = false
        resetAll()
        super.detachFromWindow()
        ScreenTranslatorService.resetForeground()
    }

    private fun resetAll() {
        StateManager.resetState()
        FloatingView.detachAllNonPrimaryViews()
        OCRManager.cancelRunningTask()
        drawAreaView = null
        ocrResultView = null
    }

    override fun onBackButtonPressed(): Boolean {
        StateManager.onBackButtonPressed()
        return super.onBackButtonPressed()
    }

    @SuppressLint("RtlHardcoded")
    override fun getLayoutGravity(): Int = Gravity.TOP or Gravity.RIGHT

    private fun setViews() {
        viewLangSelector.setOnClickListener {
            if (StateManager.state.stateName() in
                    arrayOf(StateName.ScreenshotTake,
                            StateName.OCRProcess, StateName.Translating)) {
                Utils.showErrorToast(context
                        .getString(R.string.error_canNotChangeLangWhenTranslating))
            } else {
                OCRTranslationSelectorView(context).attachToWindow()
            }
        }
        btSelectArea.setOnClickListener {
            StateManager.startSelection()
        }
        btTranslation.setOnClickListener {
            StateManager.startOCR()
        }
        btClear.setOnClickListener {
            resetAll()
        }
        viewMenu.setOnClickListener {
            FloatingBarMenu(context, viewMenu, onFloatingBarMenuCallback).show()
        }

        setupLang()
    }

    override fun onHomeButtonPressed() {
        onBackButtonPressed()
    }

    private fun setupLang(ocrLang: String = OCRLangUtil.selectLangDisplayCode,
                          transLang: String = TranslationUtil.currentTranslationLangCode) {

        val targetName = when (TranslationUtil.currentService) {
            TranslationService.GoogleTranslatorApp ->
                TranslationService.GoogleTranslatorApp.shortName
            else -> transLang
        }

        val text = "$ocrLang > $targetName"
        tvLang.text = text

        ivGoogleTranslate.setVisible(TranslationService.GoogleTranslatorApp.isCurrent)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onOCRLangChanged(event: OCRLangChangedEvent) {
        setupLang(ocrLang = event.langDisplayCode)
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTranslationLangChanged(event: TranslationLangChangedEvent) {
        setupLang(transLang = event.langCode)
    }

    init {
        StateManager.listener = object : OnStateChangedListener {

            override fun onStateChanged(state: StateName) {
                // Set up buttons

                btSelectArea.setVisible(state.equalsAny(StateName.Init, StateName.AreaSelecting))
                btSelectArea.isEnabled = state == StateName.Init

                btTranslation.setVisible(state == StateName.AreaSelected)

                pgProgress.setVisible(
                        state.equalsAny(StateName.ScreenshotTake,
                                StateName.OCRProcess,
                                StateName.Translating))

                btClear.setVisible(state == StateName.Translated)
            }

            override fun startSelection() {
                if (checkScreenshotPermission()) {
                    drawAreaView = DrawAreaView(context).apply {
                        setRealButtonHandler(this@MainBar)

                        areaSelectionView.setCallback { areaSelectionView ->
                            areaSelectionView.boxList?.let {
                                StateManager.areaSelected(it)
                            }
                        }

                        attachToWindow()
                        this@MainBar.detachFromWindow(false)
                        this@MainBar.attachToWindow()
                    }
                }
            }

            private fun clearAreaSelectionViewAndSaveArea() {
                if (SettingUtil.isRememberLastSelection) {
                    SettingUtil.lastSelectionArea = StateManager.boxList
                }
                drawAreaView?.apply {
                    areaSelectionView?.clear()
                    detachFromWindow()
                }
            }

            override fun ocrFileNotFound() {
                OCRLangUtil.showDownloadAlertDialog()
            }

            override fun beforeScreenshot() {
                clearAreaSelectionViewAndSaveArea()
                detachFromWindow(false)
            }

            override fun screenshotSuccess() {
                attachToWindow()
            }

            override fun screenshotFailed(errorCode: Int, e: Throwable) {
                attachToWindow()
                resetAll()

                val msg = when (errorCode) {
                    ScreenshotHandler.ERROR_CODE_TIMEOUT ->
                        getContext().getString(R.string.dialog_content_screenshotTimeout)
                    ScreenshotHandler.ERROR_CODE_IMAGE_FORMAT_ERROR ->
                        String.format(Locale.getDefault(),
                                getContext().getString(
                                        R.string.dialog_content_screenshotWithImageFormatError),
                                e.message)
                    else ->
                        String.format(Locale.getDefault(),
                                getContext().getString(
                                        R.string.dialog_content_screenshotWithUnknownError),
                                e.message)
                }

                showErrorDialog(DialogView.Type.CONFIRM_ONLY,
                        getContext().getString(R.string.dialog_title_error), msg)
            }

            override fun startOCR() {
                if (TranslationUtil.currentService != TranslationService.GoogleTranslatorApp) {
                    ocrResultView = OcrResultView(getContext(), onOCRResultViewCallback).apply {
                        this.setRealButtonHandler(this@MainBar)
                        this.attachToWindow()
//                    ocrResultView.setupData(screenshot, boxList)
                    }

                    detachFromWindow(false)
                    attachToWindow()
                }
            }

            private val onOCRResultViewCallback = object : OcrResultView.OnOcrResultViewCallback {
                override fun onOpenBrowserClicked() {
                }

                override fun onOpenGoogleTranslateClicked() {
                    resetAll()
                }

                override fun onOCRTextChanged(newText: String?) {
                    StateManager.changeOCRText(newText)
                }

            }

            override fun startOCRInitialization() {
                ocrResultView?.onOCRInitializing(StateManager.ocrResultList)
            }

            override fun startOCRRecognition() {
                ocrResultView?.onOCRRecognizing()
            }

            override fun ocrRecognized() {
                if (TranslationUtil.currentService == TranslationService.GoogleTranslatorApp) {
                    val text = StateManager.ocrResultText
                    GoogleTranslateUtil.start(context, Locale.getDefault().language, text)
                } else {
                    ocrResultView?.onOCRRecognized(StateManager.ocrResultList)
                }
            }

            override fun startTranslation() {
                ocrResultView?.onStartTranslation()
            }

            override fun onTranslated() {
                ocrResultView?.onTranslated()
            }

            override fun onTranslationFailed(t: Throwable?) {
                showErrorDialog(DialogView.Type.CANCEL_ONLY,
                        context.getString(R.string.title_translationFailed),
                        t?.message ?: context.getString(R.string.error_unknownError))
            }

            override fun detachResultView() {
                ocrResultView?.detachFromWindow()
            }

            override fun clearOverlay() {
                resetAll()
            }
        }
    }

    fun showErrorDialog(type: DialogView.Type, title: String, msg: String) {
        dialogView.reset()
        dialogView.setType(type)
        dialogView.setTitle(title)
        dialogView.setContentMsg(msg)
        dialogView.attachToWindow()
    }

    fun checkScreenshotPermission(): Boolean =
            if (ScreenshotHandler.isInitialized()) {
                true
            } else {
                resetAll()
                MainActivity.start(context)
                ScreenTranslatorService.stop(true)
                false
            }


    private val onFloatingBarMenuCallback = object : FloatingBarMenu.OnFloatingBarMenuCallback {

        override fun onSettingItemClick() {
            SettingView(context).attachToWindow()
        }

        override fun onAboutItemClick() {
            AboutView(context).attachToWindow()
        }

        override fun onHideItemClick() {
            this@MainBar.detachFromWindow()
        }

        override fun onCloseItemClick() {
            ScreenTranslatorService.stop(true)
        }

        override fun onHelpClick() {
            HelpView(context).attachToWindow()
        }

    }
}