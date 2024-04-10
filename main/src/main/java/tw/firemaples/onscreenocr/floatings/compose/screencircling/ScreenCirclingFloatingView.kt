package tw.firemaples.onscreenocr.floatings.compose.screencircling

import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeFloatingView
import javax.inject.Inject


class ScreenCirclingFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: ScreenCirclingViewModel,
) : ComposeFloatingView(context) {

    override val fullscreenMode: Boolean
        get() = true

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val enableHomeButtonWatcher: Boolean
        get() = true

    @Composable
    override fun RootContent() {
        ScreenCirclingContent(
            viewModel = viewModel,
        )
    }
}
