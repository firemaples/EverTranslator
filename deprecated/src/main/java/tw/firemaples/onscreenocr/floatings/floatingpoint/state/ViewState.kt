package tw.firemaples.onscreenocr.floatings.floatingpoint.state

interface ViewState {
    fun getStateEnum(): ViewStateEnum
    fun enter(manager: ViewStateManager)
}