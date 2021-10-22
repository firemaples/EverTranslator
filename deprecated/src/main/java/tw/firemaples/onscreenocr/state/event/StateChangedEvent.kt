package tw.firemaples.onscreenocr.state.event

import tw.firemaples.onscreenocr.event.BaseEvent
import tw.firemaples.onscreenocr.state.State

class StateChangedEvent(val state: State) : BaseEvent