package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.ocr.tesseract.OCRLangUtil

object AreaSelectedState : OverlayState() {
    override fun stateName(): StateName = StateName.AreaSelected

    override fun enter(manager: StateManager) {

    }

    override fun startOCR(manager: StateManager) {
        super.startOCR(manager)

        if (OCRLangUtil.checkCurrentOCRFiles()) {
            manager.cacheCurrentLangSettings()
            manager.enterState(ScreenshotTakeState)
        } else {
            manager.dispatchOCRFileNotFound()
        }
    }
}