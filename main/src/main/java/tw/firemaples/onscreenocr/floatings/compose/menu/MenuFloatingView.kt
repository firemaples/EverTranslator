package tw.firemaples.onscreenocr.floatings.compose.menu

import android.content.Context
import android.view.WindowManager
import androidx.compose.runtime.Composable
import dagger.hilt.android.qualifiers.ApplicationContext
import tw.firemaples.onscreenocr.floatings.compose.base.ComposeFloatingView
import javax.inject.Inject

class MenuFloatingView @Inject constructor(
    @ApplicationContext context: Context,
    private val viewModel: MenuViewModel,
) : ComposeFloatingView(context) {

    override val layoutWidth: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    override val layoutHeight: Int
        get() = WindowManager.LayoutParams.MATCH_PARENT

    fun getMenuViewDelegate(): MenuViewDelegate =
        this.viewModel

    @Composable
    override fun RootContent() {
        MenuContent(viewModel = viewModel)
    }
}
