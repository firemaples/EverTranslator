package tw.firemaples.onscreenocr.floatings.floatingpoint.state

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.utils.threadUI

object ViewStateManager {
    val logger: Logger = LoggerFactory.getLogger(ViewStateManager::class.java)

    var state: ViewState = ViewIdleState

    var callback: ViewStateCallback? = null

    fun enterState(nextState: ViewState) {
        logger.info("View status transition: " +
                "${state.getStateEnum().name} > ${nextState.getStateEnum().name}")
        this.state = nextState
        dispatch {
            callback?.onStateChanged(nextState.getStateEnum())
        }
        state.enter(this)
    }

    private fun dispatch(dispatcher: () -> Unit) {
        threadUI {
            dispatcher()
        }
    }
}

enum class ViewStateEnum {
    Idle
}

interface ViewStateCallback {
    fun onStateChanged(state: ViewStateEnum)
}