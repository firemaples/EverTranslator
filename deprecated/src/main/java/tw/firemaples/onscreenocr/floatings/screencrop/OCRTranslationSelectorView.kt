package tw.firemaples.onscreenocr.floatings.screencrop

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatings.FloatingView
import tw.firemaples.onscreenocr.ocr.tesseract.OCRFileUtil
import tw.firemaples.onscreenocr.ocr.tesseract.OCRLangUtil
import tw.firemaples.onscreenocr.ocr.event.TrainedDataDownloadSiteChangedEvent
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.translate.event.TranslationServiceChangedEvent
import tw.firemaples.onscreenocr.utils.*
import tw.firemaples.onscreenocr.views.MenuView

class OCRTranslationSelectorView(context: Context) : FloatingView(context) {
    private val tvTrainedDataSite: TextView = rootView.findViewById(R.id.tv_trainedDataSite)
    private val lvOcrLang: ListView = rootView.findViewById(R.id.lv_ocrLang)
    private val tvTranslationService: TextView = rootView.findViewById(R.id.tv_translationService)
    private val lvTranslationLang: ListView = rootView.findViewById(R.id.lv_translationLang)
    private val tvLangEmptyTip: TextView = rootView.findViewById(R.id.tv_langEmptyTip)
    private val btClose: View = rootView.findViewById(R.id.bt_close)

    private val ocrLangNameList: Array<String>
            by lazy { OCRLangUtil.ocrLangNameList }

    private val translationLangNameList: MutableList<String> by lazy {
        TranslationUtil.getTranslationLangNameList().toMutableList()
    }

    private val translationLangListAdapter: ArrayAdapter<String> by lazy {
        ArrayAdapter<String>(context,
                R.layout.item_lang,
                android.R.id.text1,
                translationLangNameList)
    }

    var showTranslationServiceAtNextAttached: Once<Boolean> = Once(false)

    init {
        setViews()
        setupHomeButtonWatcher(object : HomeWatcher.OnHomePressedListener {
            override fun onHomePressed() {
                detachFromWindow()
            }

            override fun onHomeLongPressed() {
            }
        })
    }

    override fun onBackButtonPressed(): Boolean {
        detachFromWindow()
        return true
    }

    override fun getLayoutId(): Int = R.layout.view_ocr_translation_selector

    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT

    private fun setViews() {
        btClose.setOnClickListener { detachFromWindow() }

        val trainedDataSites = OCRFileUtil.trainedDataSites
        tvTrainedDataSite.text = trainedDataSites[OCRFileUtil.trainedDataDownloadSiteIndex].name
        tvTrainedDataSite.setOnClickListener {
            MenuView(context = context,
                    selection = trainedDataSites.map { it.name },
                    ids = trainedDataSites.map { it.name.hashCode() },
                    listener = object : MenuView.OnMenuItemClickedListener {
                        override fun onMenuItemClicked(position: Int, id: Int, item: String) {
                            OCRFileUtil.trainedDataDownloadSiteIndex = position
                            tvTrainedDataSite.text = trainedDataSites[position].name
                        }
                    }).apply {
                marginDp = 0f
                attachToWindow(tvTrainedDataSite.getTopLeftRect())
            }
        }

        lvOcrLang.choiceMode = ListView.CHOICE_MODE_SINGLE
        lvOcrLang.adapter = ArrayAdapter<String>(context, R.layout.item_lang, android.R.id.text1, ocrLangNameList)
        lvOcrLang.select(OCRLangUtil.selectedLangIndex)
        lvOcrLang.setOnItemClickListener { _, _, position, _ ->
            OCRLangUtil.selectedLangIndex = position
            if (!OCRLangUtil.checkCurrentOCRFiles()) {
                OCRLangUtil.showDownloadAlertDialog()
            }
        }

        val translationServiceList = TranslationUtil.serviceList.asSequence().sortedBy { it.sort }.toList()
        tvTranslationService.text = translationServiceList[TranslationUtil.currentServiceIndex].fullName
        tvTranslationService.setOnClickListener {
            MenuView(context = context,
                    selection = translationServiceList.map { it.fullName },
                    ids = translationServiceList.map { it.sort },
                    listener = object : MenuView.OnMenuItemClickedListener {
                        override fun onMenuItemClicked(position: Int, id: Int, item: String) {
                            TranslationUtil.currentService = translationServiceList[position]
                            tvTranslationService.text = translationServiceList[position].fullName
                            if (TranslationUtil.currentService == TranslationService.GoogleTranslatorApp) {
                                GoogleTranslateUtil.checkInstalled(context)
                            }
                        }
                    }).attachToWindow(tvTranslationService.getTopLeftRect())
        }

        lvTranslationLang.choiceMode = ListView.CHOICE_MODE_SINGLE
        lvTranslationLang.adapter = translationLangListAdapter
        lvTranslationLang.select(TranslationUtil.currentTranslationLangIndex)
        lvTranslationLang.setOnItemClickListener { _, _, position, _ ->
            TranslationUtil.currentTranslationLangIndex = position
        }

        updateLangEmptyTip(TranslationUtil.currentService)
    }

    override fun attachToWindow() {
        super.attachToWindow()
        EventUtil.register(this)
        if (showTranslationServiceAtNextAttached.isValue(true)) {
            rootView.postDelayed({
                tvTranslationService.safePerformClick()
            }, 1000)
        }
    }

    override fun detachFromWindow() {
        super.detachFromWindow()
        EventUtil.unregister(this)
        StateManager.onLangChanged()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTrainedDataDownloadSiteChanged(event: TrainedDataDownloadSiteChangedEvent) {
        tvTrainedDataSite.text = OCRFileUtil.trainedDataSites[event.index].name
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTranslationServiceChanged(event: TranslationServiceChangedEvent) {
        translationLangNameList.apply {
            clear()
            addAll(TranslationUtil.getTranslationLangNameList(event.translationService))
        }
        translationLangListAdapter.notifyDataSetChanged()

        val index = TranslationUtil.translationLangCodeList
                .indexOf(event.translationService.defaultLangCode)
        if (index >= 0)
            lvTranslationLang.select(index)
        else
            lvTranslationLang.select(0)

        updateLangEmptyTip(event.translationService)
    }

    private fun updateLangEmptyTip(translationService: TranslationService) {
        tvLangEmptyTip.text = when (translationService) {
            TranslationService.GoogleTranslatorApp ->
                context.getString(R.string.msg_tipForChangeGoogleTranslatorLang)
            TranslationService.OCROnly ->
                context.getString(R.string.ocr_only)
            else ->
                ""
        }
    }
}