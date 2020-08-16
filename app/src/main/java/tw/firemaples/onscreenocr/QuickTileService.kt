package tw.firemaples.onscreenocr

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.event.events.ShowingStateChanged

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {
    private val logger: Logger by lazy { LoggerFactory.getLogger(QuickTileService::class.java) }

    override fun onBind(intent: Intent?): IBinder? {
        return try {
            super.onBind(intent)
        } catch (e: Exception) {
            null
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        logger.debug("onTileAdded()")
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        logger.debug("onTileRemoved()")
    }

    override fun onClick() {
        super.onClick()
        logger.debug("onClick()")

        startActivityAndCollapse(MainActivity.getShowingStateSwitchIntent(applicationContext,
                !ScreenTranslatorService.isFloatingViewShowing()))
    }

    override fun onStartListening() {
        super.onStartListening()
        logger.debug("onStartListening()")

        EventUtil.register(this)

        updateTileState()
    }

    override fun onStopListening() {
        super.onStopListening()
        logger.debug("onStopListening()")

        EventUtil.unregister(this)
    }

    private fun updateTileState(isShowing: Boolean = ScreenTranslatorService.isFloatingViewShowing()) {
        val tile = qsTile ?: return
        tile.state = if (isShowing) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }


    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onShowingStateChanged(event: ShowingStateChanged) {
        updateTileState(event.isShowing)
    }
}