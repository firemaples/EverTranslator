package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName

object ClearingState : BaseState() {
    override fun stateName(): StateName = StateName.Clearing

    override fun enter(manager: StateManager) {
        manager.dispatchClearOverlay()

        manager.enterState(InitState)
    }
}