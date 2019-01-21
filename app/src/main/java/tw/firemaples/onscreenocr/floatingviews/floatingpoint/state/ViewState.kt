package tw.firemaples.onscreenocr.floatingviews.floatingpoint.state

interface ViewState {
    fun getStateEnum(): ViewStateEnum
    fun enter(manager: ViewStateManager)
}