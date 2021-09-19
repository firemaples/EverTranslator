package tw.firemaples.onscreenocr.floatings

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import tw.firemaples.onscreenocr.floatings.main.MainBar
import tw.firemaples.onscreenocr.screenshot.ScreenshotManager
import tw.firemaples.onscreenocr.utils.Logger

class ViewHolderService : Service() {
    companion object {
        private const val ACTION_SHOW_VIEWS = "ACTION_SHOW_VIEWS"
        private const val ACTION_HIDE_VIEWS = "ACTION_HIDE_VIEWS"
        private const val ACTION_EXIT = "ACTION_EXIT"

        private val logger: Logger = Logger(ViewHolderService::class)

        fun showViews(context: Context) {
            startAction(context, ACTION_SHOW_VIEWS)
        }

        fun hideViews(context: Context) {
            startAction(context, ACTION_HIDE_VIEWS)
        }

        fun exit(context: Context) {
            startAction(context, ACTION_EXIT)
        }

        private fun startAction(context: Context, action: String) {
            context.startService(Intent(context, ViewHolderService::class.java).apply {
                this.action = action
            })
        }
    }

    private val mainBar: MainBar by lazy { MainBar(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        logger.debug("Received action: ${intent?.action}")

        return when (intent?.action) {
            ACTION_SHOW_VIEWS -> {
                showViews()
                START_STICKY
            }

            ACTION_HIDE_VIEWS -> {
                hideViews()
                START_STICKY
            }

            ACTION_EXIT -> {
                exit()
                START_NOT_STICKY
            }

            else -> {
                logger.debug("Got a unhandled action: ${intent?.action}")
                START_NOT_STICKY
            }
        }
    }

    private fun showViews() {
        mainBar.attachToScreen()
    }

    private fun hideViews() {
        mainBar.detachFromScreen()
    }

    private fun exit() {
        hideViews()
        ScreenshotManager.release()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
