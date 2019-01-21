package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName

object InitState : BaseState() {
    override fun stateName(): StateName = StateName.Init

    override fun enter(manager: StateManager) {
    }

    override fun startSelection(manager: StateManager) {
        super.startSelection(manager)
        manager.enterState(AreaSelectingState)
    }
}