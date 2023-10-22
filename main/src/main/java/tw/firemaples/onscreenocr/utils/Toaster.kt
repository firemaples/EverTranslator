package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import java.lang.ref.WeakReference

object Toaster {
    private val context: Context by lazy { Utils.context }
    private var last: WeakReference<Toast>? = null

    fun show(msg: String) {
        last?.get()?.cancel()
        last = WeakReference(Toast.makeText(context, msg, Toast.LENGTH_LONG).apply { show() })
    }

    fun show(@StringRes stringRes: Int) {
        last?.get()?.cancel()
        last = WeakReference(Toast.makeText(context, stringRes, Toast.LENGTH_LONG).apply { show() })
    }
}