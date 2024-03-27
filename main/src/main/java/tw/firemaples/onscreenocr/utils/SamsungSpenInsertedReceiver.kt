package tw.firemaples.onscreenocr.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.floatings.ViewHolderService

class SamsungSpenInsertedReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_SAMSUNG_SPEN_INSERT = "com.samsung.pen.INSERT"
        private const val EXTRA_PEN_INSERT = "penInsert"

        private val logger: Logger by lazy { Logger(SamsungSpenInsertedReceiver::class) }
        private val context: Context by lazy { CoreApplication.instance }
        private var receiver: SamsungSpenInsertedReceiver? = null
        private var isRegistered = false

        private var hasResult = false
        private var isSpenInserted = false

        @JvmStatic
        fun start() = synchronized(ACTION_SAMSUNG_SPEN_INSERT) {
            if (isRegistered) return@synchronized

            if (receiver == null) {
                receiver = SamsungSpenInsertedReceiver()
            }

            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(ACTION_SAMSUNG_SPEN_INSERT),
                ContextCompat.RECEIVER_EXPORTED,
            )
            isRegistered = true

            logger.info("started")
        }

        @JvmStatic
        fun stop() = synchronized(ACTION_SAMSUNG_SPEN_INSERT) {
            if (!isRegistered) return@synchronized

            context.unregisterReceiver(receiver)
            isRegistered = false
            hasResult = false
            isSpenInserted = false

            logger.info("stopped")
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.also {
            if (it.hasExtra(EXTRA_PEN_INSERT)) {
                val penInserted = it.getBooleanExtra(EXTRA_PEN_INSERT, false)

                if (!hasResult) {
                    hasResult = true
                } else {
                    if (penInserted != isSpenInserted) {
                        if (penInserted) {
                            ViewHolderService.exit(context)
                        }
                    }
                }
                isSpenInserted = penInserted
            }
        }
    }
}
