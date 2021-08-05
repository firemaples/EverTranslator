package tw.firemaples.onscreenocr.screenshot

import android.content.Intent

object ScreenshotManager {
    private var mediaProjectionIntent: Intent? = null

    val isGranted: Boolean
        get() = mediaProjectionIntent != null

    fun onMediaProjectionGranted(intent: Intent) {
        mediaProjectionIntent = intent.clone() as Intent
    }
}
