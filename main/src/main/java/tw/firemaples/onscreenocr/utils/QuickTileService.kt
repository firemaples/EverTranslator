package tw.firemaples.onscreenocr.utils

import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import tw.firemaples.onscreenocr.floatings.manager.FloatingStateManager
import tw.firemaples.onscreenocr.pages.launch.LaunchActivity
import tw.firemaples.onscreenocr.screenshot.ScreenExtractor

@RequiresApi(Build.VERSION_CODES.N)
class QuickTileService : TileService() {
    private val logger: Logger by lazy { Logger(QuickTileService::class) }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var listeningJob: Job? = null

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

        if (FloatingStateManager.isMainBarAttached) {
            FloatingStateManager.detachAllViews()
        } else {
            if (ScreenExtractor.isGranted) {
                FloatingStateManager.showMainBar()
            } else {
                startActivityAndCollapse(LaunchActivity.getLaunchIntent(this))
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        logger.debug("onStartListening()")

        listeningJob = scope.launch {
            FloatingStateManager.showingStateChangedFlow.collect {
                updateTileState(it)
            }
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        logger.debug("onStopListening()")

        listeningJob?.cancel()
    }

    private fun updateTileState(isShowing: Boolean) {
        logger.debug("updateTileState, isShowing: $isShowing")
        val tile = qsTile ?: return
        tile.state = if (isShowing) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }
}
