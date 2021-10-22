package tw.firemaples.onscreenocr.state

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.StateManager

abstract class BaseState : State {
    private val logger: Logger = LoggerFactory.getLogger(BaseState::class.java)

    override fun startSelection(manager: StateManager) {
        logInfo("startSelection() called")
    }

    override fun areaSelected(manager: StateManager) {
        logInfo("areaSelected() called")
    }

    override fun startOCR(manager: StateManager) {
        logInfo("startOCR() called")
    }

    override fun startTranslation(manager: StateManager) {
        logInfo("startTranslation() called")
    }

    override fun showResult(manager: StateManager) {
        logInfo("showResult() called")
    }

    override fun clear(manager: StateManager) {
        logInfo("clear() called")
    }

    override fun changeOCRText(manager: StateManager) {
        logInfo("changeOCRText() called")
    }

    override fun onLangChanged(manager: StateManager) {
        logInfo("onLangChanged() called")
    }

    private fun logInfo(msg: String) {
        logger.info("${javaClass.simpleName} -> $msg")
    }
}