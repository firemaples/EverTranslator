package tw.firemaples.onscreenocr.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import tw.firemaples.onscreenocr.CoreApplication
import tw.firemaples.onscreenocr.ScreenTranslatorService

class SamsungSpenInsertedReceiver : BroadcastReceiver() {
    companion object {
        private const val ACTION_SAMSUNG_SPEN_INSERT = "com.samsung.pen.INSERT"
        private const val EXTRA_PEN_INSERT = "penInsert"

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

            context.registerReceiver(receiver,
                    IntentFilter(ACTION_SAMSUNG_SPEN_INSERT))
            isRegistered = true
        }

        @JvmStatic
        fun stop() = synchronized(ACTION_SAMSUNG_SPEN_INSERT) {
            if (!isRegistered) return@synchronized

            context.unregisterReceiver(receiver)
            isRegistered = false
            hasResult = false
            isSpenInserted = false
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
                            ScreenTranslatorService.stop(true)
                        }
                    }
                }
                isSpenInserted = penInserted
            }
        }
    }
}