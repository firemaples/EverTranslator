package tw.firemaples.onscreenocr.floatings.mainbar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tw.firemaples.onscreenocr.R

@Composable
fun MainBarContent(
    viewModel: MainBarViewModel,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    Card(
        modifier = Modifier,
//            .background(colorResource(id = R.color.background))
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
        ) {
            MainBarButton(drawable = R.drawable.ic_selection)
            Spacer(modifier = Modifier.size(4.dp))
            MainBarButton(drawable = R.drawable.ic_translate)
            Spacer(modifier = Modifier.size(4.dp))
            MainBarButton(drawable = R.drawable.ic_close)
            Spacer(modifier = Modifier.size(4.dp))
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = onDragStart,
                            onDragEnd = onDragEnd,
                            onDragCancel = onDragCancel,
                            onDrag = onDrag,
                        )
                    },
                painter = painterResource(id = R.drawable.ic_menu_move),
                contentDescription = "",
            )
        }
    }
}

@Composable
private fun MainBarButton(
    @DrawableRes
    drawable: Int,
) {
    Image(
        modifier = Modifier
            .size(32.dp)
            .background(colorResource(id = R.color.md_blue_800), shape = RoundedCornerShape(4.dp))
            .padding(4.dp),
        painter = painterResource(id = drawable),
        contentDescription = "",
    )
}

@Preview
@Composable
private fun MainBarContentPreview() {
    val viewModel = object : MainBarViewModel {
        override fun onMenuItemClicked(key: String) = Unit
        override fun onSelectClicked() = Unit
        override fun onTranslateClicked() = Unit
        override fun onCloseClicked() = Unit
        override fun onMenuButtonClicked() = Unit
        override fun onAttachedToScreen() = Unit
        override fun saveLastPosition(x: Int, y: Int) = Unit
    }

    MainBarContent(viewModel = viewModel,
        onDragStart = { offset -> },
        onDragEnd = {},
        onDragCancel = {},
        onDrag = { change, dragAmount -> })
}