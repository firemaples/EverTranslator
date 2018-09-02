package tw.firemaples.onscreenocr

import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.experimental.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.ocr.OcrResult
import tw.firemaples.onscreenocr.state.InitState
import tw.firemaples.onscreenocr.state.State
import tw.firemaples.onscreenocr.state.event.StateChangedEvent
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil
import tw.firemaples.onscreenocr.utils.stateManagerAction
import tw.firemaples.onscreenocr.utils.threadUI
import java.util.*

object StateManager {
    var logger: Logger = LoggerFactory.getLogger(StateManager::class.java)

    var state: State = InitState

    var listener: OnStateChangedListener? = null

    var boxList: MutableList<Rect> = arrayListOf()
    var bitmap: Bitmap? = null
    var ocrResultList: MutableList<OcrResult> = ArrayList()
    var ocrResultText: String?
        get() = if (ocrResultList.isNotEmpty()) ocrResultList.first().text else null
        set(value) {
            if (ocrResultList.isNotEmpty()) ocrResultList.first().text = value
        }
    var translatedText: String?
        get() = if (ocrResultList.isNotEmpty()) ocrResultList.first().translatedText else null
        set(value) {
            if (ocrResultList.isNotEmpty()) ocrResultList.first().translatedText = value
        }
    var cachedOCRLangCode: String? = null
    var cachedTranslateService: TranslationService? = null
    var cachedTranslationLangCode: String? = null

    fun resetState() {
        enterState(InitState)
    }

    fun clear() {
        boxList.clear()
        bitmap = null
        ocrResultList.clear()
    }

    fun enterState(nextState: State) {
        logger.info("State transition: ${state.stateName()} > ${nextState.stateName()}")
        state = nextState
        dispatch {
            listener?.onStateChanged(nextState.stateName())
        }
        EventUtil.postSticky(StateChangedEvent(nextState))
        state.enter(this)
    }

    fun cacheCurrentLangSettings() {
        cachedOCRLangCode = OCRLangUtil.selectedLangCode
        cachedTranslateService = TranslationUtil.currentService
        cachedTranslationLangCode = TranslationUtil.currentTranslationLangCode
    }

    // Action dispatchers
    fun startSelection() = doAction {
        state.startSelection(this@StateManager)
    }

    fun areaSelected(boxList: MutableList<Rect>) = doAction {
        this@StateManager.boxList = boxList.toMutableList()
        state.areaSelected(this@StateManager)
    }

    fun startOCR() = doAction {
        state.startOCR(this@StateManager)
    }

    fun changeOCRText(newText: String) = doAction {
        ocrResultText = newText
        state.changeOCRText(this@StateManager)
    }

    fun onLangChanged() = doAction {
        state.onLangChanged(this@StateManager)
    }

    fun onBackButtonPressed() = doAction {
        state.clear(this@StateManager)
    }

    private fun doAction(action: () -> Unit) = launch(stateManagerAction) {
        action()
    }

    // Callback dispatchers

    fun dispatchStartSelection() = dispatch { listener?.startSelection() }

    fun dispatchOCRFileNotFound() = dispatch {
        listener?.ocrFileNotFound()
    }

    fun dispatchBeforeScreenshot() = dispatch {
        listener?.beforeScreenshot()
    }

    fun dispatchScreenshotSuccess() = dispatch {
        listener?.screenshotSuccess()
    }

    fun dispatchScreenshotFailed(errorCode: Int, e: Throwable) = dispatch {
        listener?.screenshotFailed(errorCode, e)
    }

    fun dispatchStartOCR() = dispatch {
        listener?.startOCR()
    }

    fun dispatchStartOCRInitializing() = dispatch {
//        ocrResultList.clear()
//        for (rect in boxList) {
//            val ocrResult = OcrResult()
//            ocrResult.rect = rect
//            val rects = ArrayList<Rect>()
//            rects.add(Rect(0, 0, rect.width(), rect.height()))
//            ocrResult.boxRects = rects
//            ocrResultList.add(ocrResult)
//        }

        listener?.startOCRInitialization()
    }

    fun dispatchStartOCRRecognizing() = dispatch {
        listener?.startOCRRecognition()
    }

    fun dispatchOCRRecognized() = dispatch {
        listener?.ocrRecognized()
    }

    fun dispatchStartTranslation() = dispatch {
        listener?.startTranslation()
    }

    fun dispatchOnTranslated() = dispatch {
        listener?.onTranslated()
    }

    fun dispatchTranslationFailed(t: Throwable?) {
        listener?.onTranslationFailed(t)
    }

    fun dispatchDetachResultView() = dispatch {
        listener?.detachResultView()
    }

    fun dispatchClearOverlay() = dispatch { listener?.clearOverlay() }

    private fun dispatch(dispatcher: () -> Unit) =
            threadUI {
                dispatcher()
            }
}

enum class StateName {
    Init, AreaSelecting, AreaSelected, ScreenshotTake, OCRProcess, Translating, Translated, Clearing
}

interface OnStateChangedListener {
    fun onStateChanged(state: StateName)

    fun clearOverlay()
    fun startSelection()
    fun ocrFileNotFound()
    fun beforeScreenshot()
    fun screenshotSuccess()
    fun screenshotFailed(errorCode: Int, e: Throwable)
    fun startOCR()
    fun startOCRInitialization()
    fun startOCRRecognition()
    fun ocrRecognized()
    fun startTranslation()
    fun onTranslated()
    fun detachResultView()
    fun onTranslationFailed(t: Throwable?)
}