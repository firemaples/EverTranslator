package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName

object AreaSelectingState : OverlayState() {
    override fun stateName(): StateName = StateName.AreaSelecting

    override fun enter(manager: StateManager) {
        manager.dispatchStartSelection()
    }

    override fun areaSelected(manager: StateManager) {
        super.areaSelected(manager)
        manager.enterState(AreaSelectedState)
    }
}