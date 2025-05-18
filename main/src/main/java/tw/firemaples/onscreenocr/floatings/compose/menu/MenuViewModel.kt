package tw.firemaples.onscreenocr.floatings.compose.menu

import android.graphics.Rect
import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

interface MenuViewModel : MenuViewDelegate {
    val state: StateFlow<MenuState>
    fun onMenuClicked(key: String)
    fun onMenuOutsideClicked()
}

interface MenuViewDelegate {
    fun setOnMenuItemClickedListener(onClicked: (key: String?) -> Unit)
    fun setMenuData(menuItems: List<MenuItem>)
    fun setAnchor(anchor: Rect)
}

data class MenuItem(val key: String, val text: String, val selected: Boolean = false)

@Stable
data class MenuState(
    val anchor: Rect = Rect(),
    val menuItems: List<MenuItem> = listOf(),
)

class MenuViewModelImpl @Inject constructor() : MenuViewModel {

    override val state = MutableStateFlow(MenuState())

    private var onMenuItemClicked: ((key: String?) -> Unit)? = null

    override fun setOnMenuItemClickedListener(onClicked: (key: String?) -> Unit) {
        this.onMenuItemClicked = onClicked
    }

    override fun setMenuData(menuItems: List<MenuItem>) {
        state.update {
            it.copy(
                menuItems = menuItems,
            )
        }
    }

    override fun setAnchor(anchor: Rect) {
        state.update {
            it.copy(anchor = anchor)
        }
    }

    override fun onMenuClicked(key: String) {
        onMenuItemClicked?.invoke(key)
    }

    override fun onMenuOutsideClicked() {
        onMenuItemClicked?.invoke(null)
    }
}
