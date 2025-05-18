package tw.firemaples.onscreenocr.floatings.compose.menu

import android.graphics.Rect
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.compose.base.PreviewThemes
import tw.firemaples.onscreenocr.floatings.compose.base.calculateOffset
import tw.firemaples.onscreenocr.floatings.compose.base.clickableWithoutRipple
import tw.firemaples.onscreenocr.floatings.compose.base.dpToPx
import tw.firemaples.onscreenocr.theme.AppTheme

@Composable
fun MenuContent(viewModel: MenuViewModel) {
    val state by viewModel.state.collectAsState()
    val emptyInteractionSource = remember { MutableInteractionSource() }

    val offset = remember {
        mutableStateOf(IntOffset(20, 0))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.dialogOutside))
            .clickableWithoutRipple(
                interactionSource = emptyInteractionSource,
                onClick = viewModel::onMenuOutsideClicked,
            ),
    ) {
        Column(
            modifier = Modifier
                .calculateOffset(
                    anchor = state.anchor,
                    offset = offset,
                    verticalSpacing = 4.dp.dpToPx(),
                )
                .offset { offset.value }
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                )
                .width(IntrinsicSize.Max)
                .animateContentSize(),
        ) {
            state.menuItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .clickable(onClick = { viewModel.onMenuClicked(item.key) })
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (item.selected) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.size(2.dp))
                    }
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.text,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@PreviewThemes
@Composable
private fun MenuContentPreview() {
    val state = MenuState(
        menuItems = listOf(
            MenuItem(
                key = "1", text = "Menu Item 1", selected = true,
            ),
            MenuItem(
                key = "2", text = "Menu Item 2", selected = false,
            ),
            MenuItem(
                key = "3", text = "Menu Item 3", selected = false,
            ),
        )
    )

    val viewModel = object : MenuViewModel {
        override val state = MutableStateFlow(state)
        override fun onMenuClicked(key: String) = Unit
        override fun onMenuOutsideClicked() = Unit
        override fun setOnMenuItemClickedListener(onClicked: (key: String?) -> Unit) = Unit
        override fun setMenuData(menuItems: List<MenuItem>) = Unit
        override fun setAnchor(anchor: Rect) = Unit
    }

    AppTheme {
        MenuContent(viewModel = viewModel)
    }
}
