package tw.firemaples.onscreenocr.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

object Toaster {
    private val context: Context by lazy { Utils.context }

    fun show(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun show(@StringRes stringRes: Int) {
        Toast.makeText(context, stringRes, Toast.LENGTH_SHORT).show()
    }
}