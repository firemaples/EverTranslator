package tw.firemaples.onscreenocr.floatings.compose.mainbar

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.compose.base.AppColorScheme
import tw.firemaples.onscreenocr.floatings.compose.base.AppTheme

@Composable
fun MainBarContent(
    viewModel: MainBarViewModel,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .background(
                color = AppColorScheme.background,
                shape = RoundedCornerShape(8.dp),
            ),
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
        ) {
            LanguageBlock(
                langText = state.langText,
                translatorIcon = state.translatorIcon,
                onClick = viewModel::onLanguageBlockClicked,
            )
            if (state.displaySelectButton) {
                Spacer(modifier = Modifier.size(4.dp))
                MainBarButton(
                    icon = R.drawable.ic_selection,
                    onClick = viewModel::onSelectClicked,
                )
            }
            if (state.displayTranslateButton) {
                Spacer(modifier = Modifier.size(4.dp))
                MainBarButton(
                    icon = R.drawable.ic_translate,
                    onClick = viewModel::onTranslateClicked,
                )
            }
            if (state.displayCloseButton) {
                Spacer(modifier = Modifier.size(4.dp))
                MainBarButton(
                    icon = R.drawable.ic_close,
                    onClick = viewModel::onCloseClicked,
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
            MenuButton(
                onClick = viewModel::onMenuButtonClicked,
                onDragStart = onDragStart,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                onDrag = onDrag,
            )
        }
    }
}

@Composable
private fun LanguageBlock(
    langText: String,
    translatorIcon: Int? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .border(
                width = 2.dp,
                color = AppColorScheme.onBackground,
                shape = RoundedCornerShape(4.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = langText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = AppColorScheme.onBackground,
        )
        if (translatorIcon != null) {
            Image(
                painter = painterResource(id = translatorIcon),
                contentDescription = "",
                colorFilter = ColorFilter.tint(AppColorScheme.onBackground),
            )
        }
    }
}

@Composable
private fun MainBarButton(
    @DrawableRes
    icon: Int,
    onClick: () -> Unit,
) {
    Image(
        modifier = Modifier
            .size(32.dp)
            .clickable(onClick = onClick)
            .background(colorResource(id = R.color.md_blue_800), shape = RoundedCornerShape(4.dp))
            .padding(4.dp),
        painter = painterResource(id = icon),
        contentDescription = "",
    )
}

@Composable
private fun MenuButton(
    onClick: () -> Unit,
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit,
) {
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
            }
            .clickable(onClick = onClick)
            .padding(2.dp),
        painter = painterResource(id = R.drawable.ic_menu_move),
        colorFilter = ColorFilter.tint(AppColorScheme.onBackground),
        contentDescription = "",
    )
}

private class MainBarStateProvider : PreviewParameterProvider<MainBarState> {
    override val values: Sequence<MainBarState>
        get() = listOf(
            MainBarState(
                langText = "en>",
                translatorIcon = R.drawable.ic_google_translate_dark_grey,
                displaySelectButton = true,
                displayTranslateButton = true,
                displayCloseButton = true,
            ),
            MainBarState(
                langText = "en>tw",
                translatorIcon = null,
                displaySelectButton = true,
                displayTranslateButton = true,
                displayCloseButton = true,
            )
        ).asSequence()

}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MainBarContentPreview(
    @PreviewParameter(MainBarStateProvider::class) state: MainBarState,
) {
    val viewModel = object : MainBarViewModel {
        override val state: StateFlow<MainBarState>
            get() = MutableStateFlow(state)
        override val action: SharedFlow<MainBarAction>
            get() = MutableSharedFlow()

        override fun onMenuItemClicked(key: String) = Unit
        override fun onSelectClicked() = Unit
        override fun onTranslateClicked() = Unit
        override fun onCloseClicked() = Unit
        override fun onMenuButtonClicked() = Unit
        override fun onAttachedToScreen() = Unit
        override fun saveLastPosition(x: Int, y: Int) = Unit
        override fun onLanguageBlockClicked() = Unit
    }

    AppTheme {
        MainBarContent(viewModel = viewModel,
            onDragStart = { offset -> },
            onDragEnd = {},
            onDragCancel = {},
            onDrag = { change, dragAmount -> })
    }

}