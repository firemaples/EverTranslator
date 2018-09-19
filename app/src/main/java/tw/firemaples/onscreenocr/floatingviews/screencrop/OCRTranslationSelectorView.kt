package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.ocr.OCRFileUtil
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.translate.GoogleTranslateUtil
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.translate.event.TranslationServiceChangedEvent
import tw.firemaples.onscreenocr.utils.HomeWatcher
import tw.firemaples.onscreenocr.utils.isSkipNextSelect
import tw.firemaples.onscreenocr.utils.select
import tw.firemaples.onscreenocr.utils.skipNextSelect

class OCRTranslationSelectorView(context: Context) : FloatingView(context) {
    private val spTrainedDataSite: Spinner = rootView.findViewById(R.id.sp_trainedDataSite)
    private val lvOcrLang: ListView = rootView.findViewById(R.id.lv_ocrLang)
    private val spTranslationService: Spinner = rootView.findViewById(R.id.sp_translationService)
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

    override fun layoutFocusable(): Boolean {
        return true
    }

    override fun onBackButtonPressed(): Boolean {
        detachFromWindow()
        return true
    }

    override fun getLayoutId(): Int = R.layout.view_ocr_translation_selector

    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT

    private fun setViews() {
        btClose.setOnClickListener { detachFromWindow() }

        spTrainedDataSite.adapter = ArrayAdapter<String>(context,
                R.layout.item_spinner,
                android.R.id.text1,
                OCRFileUtil.trainedDataSites.map { it.name }).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spTrainedDataSite.skipNextSelect()
        spTrainedDataSite.setSelection(OCRFileUtil.trainedDataDownloadSiteIndex)
        spTrainedDataSite.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent?.isSkipNextSelect(true) == true) return
                OCRFileUtil.trainedDataDownloadSiteIndex = position
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

        spTranslationService.adapter = ArrayAdapter<String>(context,
                R.layout.item_spinner,
                android.R.id.text1,
                TranslationUtil.serviceList.map { it.fullName }).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spTranslationService.skipNextSelect()
        spTranslationService.setSelection(TranslationUtil.currentServiceIndex)
        spTranslationService.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent?.isSkipNextSelect(true) == true) return
                TranslationUtil.currentService = TranslationUtil.serviceList.first { it.sort == position }
                if (TranslationUtil.currentService == TranslationService.GoogleTranslatorApp) {
                    GoogleTranslateUtil.checkInstalled(context)
                }
            }
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
    }

    override fun detachFromWindow() {
        super.detachFromWindow()
        EventUtil.unregister(this)
        StateManager.onLangChanged()
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
        when (translationService) {
            TranslationService.GoogleTranslatorApp ->
                tvLangEmptyTip.text = context.getString(R.string.msg_tipForChangeGoogleTranslatorLang)
            else ->
                tvLangEmptyTip.text = null
        }
    }
}