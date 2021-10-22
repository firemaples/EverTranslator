package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager

abstract class OverlayState : BaseState() {
    override fun clear(manager: StateManager) {
        super.clear(manager)
        manager.enterState(ClearingState)
    }
}