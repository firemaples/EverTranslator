package tw.firemaples.onscreenocr.floatings

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import tw.firemaples.onscreenocr.floatings.main.MainBar
import tw.firemaples.onscreenocr.utils.Logger

class ViewHolderService : Service() {
    companion object {
        private const val ACTION_SHOW_VIEWS = "ACTION_SHOW_VIEWS"
        private const val ACTION_HIDE_VIEWS = "ACTION_HIDE_VIEWS"

        private val logger: Logger = Logger(ViewHolderService::class)

        fun showViews(context: Context) {
            context.startService(Intent(context, ViewHolderService::class.java).apply {
                action = ACTION_SHOW_VIEWS
            })
        }

        fun hideViews(context: Context) {
            context.startService(Intent(context, ViewHolderService::class.java).apply {
                action = ACTION_HIDE_VIEWS
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

            else -> START_NOT_STICKY
        }
    }

    private fun showViews() {
        mainBar.attachToScreen()
    }

    private fun hideViews() {
        mainBar.detachFromScreen()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
