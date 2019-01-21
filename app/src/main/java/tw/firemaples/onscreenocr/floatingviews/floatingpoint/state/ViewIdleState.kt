package tw.firemaples.onscreenocr.floatingviews.floatingpoint.state

object ViewIdleState : ViewBaseState() {
    override fun getStateEnum(): ViewStateEnum = ViewStateEnum.Idle

    override fun enter(manager: ViewStateManager) {
        
    }
}