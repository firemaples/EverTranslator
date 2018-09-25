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
import tw.firemaples.onscreenocr.remoteconfig.RemoteConfigUtil
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler
import tw.firemaples.onscreenocr.state.InitState
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.translate.event.InstallGoogleTranslatorEvent
import tw.firemaples.onscreenocr.translate.event.TranslationLangChangedEvent
import tw.firemaples.onscreenocr.utils.*
import tw.firemaples.onscreenocr.views.FloatingBarMenu
import java.util.*

class MainBar(context: Context) : MovableFloatingView(context), RealButtonHandler {
    private val logger: Logger = LoggerFactory.getLogger(MainBar::class.java)

    private val viewLangSelector: View = rootView.findViewById(R.id.view_langSelector)
    private val tvLang: TextView = rootView.findViewById(R.id.tv_lang)
    private val ivGoogleTranslate: View = rootView.findViewById(R.id.iv_googleTranslate)
    private val btSelectArea: View = rootView.findViewById(R.id.bt_selectArea)
    private val btTranslation: View = rootView.findViewById(R.id.bt_translation)
    private val btClear: View = rootView.findViewById(R.id.bt_clear)
    private val pgProgress: View = rootView.findViewById(R.id.pg_progress)
    private val viewMenu: View = rootView.findViewById(R.id.view_menu)

    private var ocrTranslationSelectorView: OCRTranslationSelectorView? = null
    private var drawAreaView: DrawAreaView? = null
    private var ocrResultView: OCRResultView? = null
    private val dialogView: DialogView by lazy { DialogView(context) }

    private val tempDisableAutoAreaSelecting = Once(false)

    init {
        setViews()
        setDragView(viewMenu)
    }

    override fun isPrimaryView(): Boolean {
        return true
    }

    override fun getLayoutId(): Int = R.layout.view_floating_bar_new

    override fun enableTransparentWhenMoved(): Boolean =
            StateManager.state is InitState

    override fun attachToWindow() {
        super.attachToWindow()
        SettingUtil.isAppShowing = true

        if (!SettingUtil.isReadmeAlreadyShown) {
            HelpView(context).attachToWindow()
            tempDisableAutoAreaSelecting.setValue(true)
        }

        if (!SettingUtil.isVersionHistoryAlreadyShown) {
            VersionHistoryView(context).attachToWindow()
            tempDisableAutoAreaSelecting.setValue(true)
        }

        tempDisableAutoAreaSelecting.whenNotValue(true) {
            if (SettingUtil.startingWithSelectionMode && StateManager.state == InitState) {
                StateManager.startSelection()
            }
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
        ocrTranslationSelectorView?.detachFromWindow()
        FloatingView.detachAllNonPrimaryViews()
        OCRManager.cancelRunningTask()
        drawAreaView = null
        ocrResultView = null
    }

    private fun showView(show: Boolean) = rootView.setVisible(show)

    override fun onBackButtonPressed(): Boolean {
        StateManager.onBackButtonPressed()
        rescheduleFadeOut()
        return super.onBackButtonPressed()
    }

    @SuppressLint("RtlHardcoded")
    override fun getLayoutGravity(): Int = Gravity.TOP or Gravity.RIGHT

    private fun setViews() {
        viewLangSelector.setOnClickListener {
            rescheduleFadeOut()
            if (StateManager.state.stateName() in
                    arrayOf(StateName.ScreenshotTake,
                            StateName.OCRProcess, StateName.Translating)) {
                Utils.showErrorToast(context
                        .getString(R.string.error_canNotChangeLangWhenTranslating))
            } else {
                ocrTranslationSelectorView = OCRTranslationSelectorView(context).apply {
                    attachToWindow()
                }
            }
        }
        btSelectArea.setOnClickListener {
            rescheduleFadeOut()
            StateManager.startSelection()
        }
        btTranslation.setOnClickListener {
            if (SettingUtil.isRememberLastSelection) {
                SettingUtil.lastSelectionArea = StateManager.boxList
            }
            if (TranslationUtil.currentService == TranslationService.GoogleTranslatorApp &&
                    !GoogleTranslateUtil.checkInstalled(context)) {
                return@setOnClickListener
            }
            rescheduleFadeOut()
            StateManager.startOCR()
        }
        btClear.setOnClickListener {
            rescheduleFadeOut()
            resetAll()
        }
        viewMenu.setOnClickListener {
            rescheduleFadeOut()
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

        val text = "$ocrLang>$targetName"
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

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInstallGoogleTranslate(event: InstallGoogleTranslatorEvent) {
        resetAll()
    }

    init {
        StateManager.listener = object : OnStateChangedListener {

            override fun onStateChanged(state: StateName) {
                rescheduleFadeOut()

                when (state) {
                    StateName.Translating, StateName.Translated -> {
                        showView(false)
                    }
                    else -> {
                        showView(true)
                    }
                }

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

            private fun clearAreaSelectionView() {
                drawAreaView?.apply {
                    areaSelectionView?.clear()
                    detachFromWindow()
                }
            }

            override fun ocrFileNotFound() {
                OCRLangUtil.showDownloadAlertDialog()
            }

            override fun beforeScreenshot() {
                clearAreaSelectionView()
                detachFromWindow(false)
            }

            override fun screenshotSuccess() {
                attachToWindow()
            }

            override fun screenshotFailed(errorCode: Int, e: Throwable?) {
                attachToWindow()
                resetAll()
                rescheduleFadeOut()

                val msg = when (errorCode) {
                    ScreenshotHandler.ERROR_CODE_TIMEOUT ->
                        getContext().getString(R.string.dialog_content_screenshotTimeout)
                    ScreenshotHandler.ERROR_CODE_IMAGE_FORMAT_ERROR ->
                        String.format(Locale.getDefault(),
                                getContext().getString(
                                        R.string.dialog_content_screenshotWithImageFormatError),
                                e?.message ?: "Unknown")
                    else ->
                        String.format(Locale.getDefault(),
                                getContext().getString(
                                        R.string.dialog_content_screenshotWithUnknownError),
                                e?.message ?: "Unknown")
                }

                showErrorDialog(DialogView.Type.CONFIRM_ONLY,
                        getContext().getString(R.string.dialog_title_error), msg)
            }

            override fun startOCR() {
                if (TranslationUtil.currentService != TranslationService.GoogleTranslatorApp) {
                    ocrResultView = OCRResultView(getContext()).apply {
                        this.setRealButtonHandler(this@MainBar)
                        this.attachToWindow()
                    }

                    detachFromWindow(false)
                    attachToWindow()
                }
            }

            override fun startOCRInitialization() {
            }

            override fun startOCRRecognition() {
            }

            override fun ocrRecognized() {
                if (TranslationUtil.currentService == TranslationService.GoogleTranslatorApp) {
                    StateManager.ocrResultText?.let { textToTranslate ->
                        FabricUtils.logTranslationInfo(textToTranslate, "",
                                TranslationService.GoogleTranslatorApp)
                        GoogleTranslateUtil.start(context, Locale.getDefault().language,
                                textToTranslate)
                    }
                }
            }

            override fun startTranslation() {
            }

            override fun onTranslated() {
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
                rescheduleFadeOut()
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
        override fun onPrivacyPolicyClick() {
            resetAll()
            Utils.openBrowser(RemoteConfigUtil.privacyPolicyUrl)
        }

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