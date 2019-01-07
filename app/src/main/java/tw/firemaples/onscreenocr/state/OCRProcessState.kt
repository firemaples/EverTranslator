package tw.firemaples.onscreenocr.state

import android.graphics.Rect
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.ocr.OCRManager
import tw.firemaples.onscreenocr.ocr.OcrResult
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil

object OCRProcessState : OverlayState() {
    var manager: StateManager? = null

    override fun stateName(): StateName = StateName.OCRProcess

    override fun enter(manager: StateManager) {
        this.manager = manager

        manager.dispatchStartOCR()

        OCRManager.setListener(callback)

        manager.ocrResultList.clear()
        for (rect in manager.boxList) {
            val ocrResult = OcrResult()
            ocrResult.rect = rect
            val rectList = ArrayList<Rect>()
            rectList.add(Rect(0, 0, rect.width(), rect.height()))
            ocrResult.boxRects = rectList
            manager.ocrResultList.add(ocrResult)
        }

        OCRManager.start(manager.bitmap!!, manager.boxList)
    }

    val callback = object : OCRManager.OnOCRStateChangedListener {
        override fun onInitializing() {
            FirebaseEvent.logStartOCRInitializing()
            manager?.dispatchStartOCRInitializing()
        }

        override fun onInitialized() {
            FirebaseEvent.logOCRInitialized()
        }

        override fun onRecognizing() {
            FirebaseEvent.logStartOCR()
            manager?.dispatchStartOCRRecognizing()
        }

        override fun onRecognized(results: List<OcrResult>) {
            FirebaseEvent.logOCRFinished()
            this@OCRProcessState.onRecognized(results)
        }
    }

    fun onRecognized(results: List<OcrResult>) {
        manager?.ocrResultList?.apply {
            clear()
            addAll(results)

            manager?.apply {
                dispatchOCRRecognized()
                if (TranslationUtil.currentService != TranslationService.GoogleTranslatorApp) {
                    enterState(TranslatingState)
                } else {
                    enterState(InitState)
                }
            }
        }
    }
}