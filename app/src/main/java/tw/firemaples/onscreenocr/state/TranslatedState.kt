package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.ocr.OCRLangUtil
import tw.firemaples.onscreenocr.translate.TranslationUtil

object TranslatedState : OverlayState() {
    override fun stateName(): StateName = StateName.Translated

    override fun enter(manager: StateManager) {

    }

    override fun changeOCRText(manager: StateManager) {
        super.changeOCRText(manager)
        manager.enterState(TranslatingState)
    }

    override fun onLangChanged(manager: StateManager) {
        super.onLangChanged(manager)

        when {
            manager.cachedOCRLangCode != OCRLangUtil.selectedLangCode -> {
                manager.dispatchDetachResultView()
                manager.enterState(AreaSelectedState)
                manager.startOCR()
            }
            manager.cachedTranslateService != TranslationUtil.currentService -> {
                manager.dispatchDetachResultView()
                manager.enterState(AreaSelectedState)
                manager.startOCR()
            }
            manager.cachedTranslationLangCode != TranslationUtil.currentTranslationLangCode -> {
                manager.enterState(TranslatingState)
            }
        }

        manager.cacheCurrentLangSettings()
    }
}