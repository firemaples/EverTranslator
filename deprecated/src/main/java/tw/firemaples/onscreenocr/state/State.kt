package tw.firemaples.onscreenocr.state

import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName

interface State {
    fun stateName(): StateName

    fun enter(manager: StateManager)

    fun startSelection(manager: StateManager)

    fun areaSelected(manager: StateManager)

    fun startOCR(manager: StateManager)

    fun startTranslation(manager: StateManager)

    fun showResult(manager: StateManager)

    fun clear(manager: StateManager)

    fun changeOCRText(manager: StateManager)

    fun onLangChanged(manager: StateManager)
}