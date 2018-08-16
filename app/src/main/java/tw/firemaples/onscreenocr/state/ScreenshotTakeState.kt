package tw.firemaples.onscreenocr.state

import android.graphics.Bitmap
import tw.firemaples.onscreenocr.StateManager
import tw.firemaples.onscreenocr.StateName
import tw.firemaples.onscreenocr.screenshot.ScreenshotHandler

object ScreenshotTakeState : BaseState() {
    var manager: StateManager? = null

    override fun stateName(): StateName = StateName.ScreenshotTake

    override fun enter(manager: StateManager) {
        this.manager = manager

        val screenshotHandler = ScreenshotHandler.getInstance()
        if (screenshotHandler.isGetUserPermission) {
            screenshotHandler.setCallback(onScreenshotHandlerCallback)

            screenshotHandler.takeScreenshot(100)
        } else {
            screenshotHandler.getUserPermission()
        }
    }

    private val onScreenshotHandlerCallback = object : ScreenshotHandler.OnScreenshotHandlerCallback {
        override fun onScreenshotStart() {
            manager?.dispatchBeforeScreenshot()
        }

        override fun onScreenshotFinished(bitmap: Bitmap) {
            manager?.apply {
                this.bitmap = bitmap
                dispatchScreenshotSuccess()
                enterState(OCRProcessState)
            }
        }

        override fun onScreenshotFailed(errorCode: Int, e: Throwable) {
            manager?.dispatchScreenshotFailed(errorCode, e)
        }
    }
}