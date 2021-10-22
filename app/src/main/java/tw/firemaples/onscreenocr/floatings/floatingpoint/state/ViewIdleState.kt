package tw.firemaples.onscreenocr.floatings.floatingpoint.state

object ViewIdleState : ViewBaseState() {
    override fun getStateEnum(): ViewStateEnum = ViewStateEnum.Idle

    override fun enter(manager: ViewStateManager) {
        
    }
}