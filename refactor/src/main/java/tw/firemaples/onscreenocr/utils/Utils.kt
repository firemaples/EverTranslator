package tw.firemaples.onscreenocr.utils

import android.content.Context
import tw.firemaples.onscreenocr.CoreApplication

class Utils {
    companion object {
        val context: Context by lazy { CoreApplication.instance }
    }
}
