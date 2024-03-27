package tw.firemaples.onscreenocr.wigets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import tw.firemaples.onscreenocr.utils.Logger

/**
 * Reference: http://stackoverflow.com/a/31340960/2906153
 */
class HomeButtonWatcher(
    val context: Context,
    var onHomeButtonPressed: (() -> Unit)? = null,
    var onHomeButtonLongPressed: (() -> Unit)? = null
) {
    private val logger: Logger by lazy { Logger(this::class) }

    private val filter: IntentFilter by lazy { IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }

    private val receiver: InnerReceiver by lazy {
        InnerReceiver(
            homePressed = { onHomeButtonPressed?.invoke() },
            homeLongPressed = { onHomeButtonLongPressed?.invoke() },
        )
    }

    private var watching: Boolean = false

    fun startWatch() {
        if (watching) return

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        watching = true
    }

    fun stopWatch() {
        if (!watching) return

        try {
            context.unregisterReceiver(receiver)
        } catch (e: Exception) {
            logger.warn(t = e)
        }
        watching = false
    }

    private class InnerReceiver(
        val homePressed: (() -> Unit)? = null,
        val homeLongPressed: (() -> Unit)? = null
    ) : BroadcastReceiver() {
        companion object {
            private const val SYSTEM_DIALOG_REASON_KEY = "reason"
            private const val SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions"
            private const val SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps"
            private const val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"

            private val logger: Logger by lazy { Logger(this::class) }
        }

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            if (action != Intent.ACTION_CLOSE_SYSTEM_DIALOGS) return

            val reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY)
            logger.debug("HomeButtonWatcher, action: $action, reason: $reason")

            when (reason) {
                SYSTEM_DIALOG_REASON_HOME_KEY -> homePressed?.invoke()
                SYSTEM_DIALOG_REASON_RECENT_APPS -> homeLongPressed?.invoke()
            }
        }
    }
}
