package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.log.FirebaseEvent
import tw.firemaples.onscreenocr.translate.TranslationManager
import tw.firemaples.onscreenocr.translate.TranslationService
import tw.firemaples.onscreenocr.translate.TranslationUtil

object TranslatingState : OverlayState() {
    private var service: TranslationService? = null

    override fun stateName(): StateName = StateName.Translating

    override fun enter(manager: StateManager) {
        manager.dispatchStartTranslation()
        if (manager.ocrResultList.isEmpty() || manager.ocrResultText.isNullOrBlank()) {
            onTranslated(manager, "")
        } else {
            service = TranslationUtil.currentService

            TranslationManager.startTranslation(manager.ocrResultText ?: "",
                    TranslationUtil.currentTranslationLangCode) { isSuccess, result, throwable ->
                if (isSuccess) {
                    onTranslated(manager, result)
                } else {
                    onTranslationFailed(manager, throwable)
                }
            }
        }
    }

    private fun onTranslated(manager: StateManager, text: String) {
        FirebaseEvent.logTranslationTextFinished(service)
        manager.translatedText = text
        manager.dispatchOnTranslated()
        manager.enterState(TranslatedState)
    }

    private fun onTranslationFailed(manager: StateManager, t: Throwable?) {
        FirebaseEvent.logTranslationTextFailed(service)
        t?.also { FirebaseEvent.logException(t) }
        manager.translatedText = ""
        manager.dispatchTranslationFailed(t)
        manager.enterState(TranslatedState)
    }

    override fun changeOCRText(manager: StateManager) {
        super.changeOCRText(manager)
        //TODO cancel current translation task
        manager.enterState(TranslatingState)
    }
}