package tw.firemaples.onscreenocr.floatings.compose.base

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first

@Composable
fun <T> Flow<T>.collectOnLifecycleResumed(state: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(this, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            this@collectOnLifecycleResumed.collect(state)
        }
    }
}

suspend fun <T> MutableSharedFlow<T>.awaitForSubscriber() {
    subscriptionCount.first { it > 0 }
}

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun Modifier.clickableWithoutRipple(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit
) = then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = { onClick() }
    )
)

fun Modifier.calculateOffset(
    anchor: Rect,
    offset: MutableState<IntOffset>,
    viewPadding: Float = 0f,
    verticalSpacing: Float = 0f,
): Modifier = onGloballyPositioned { coordinates ->
    val parent = coordinates.parentLayoutCoordinates?.size ?: return@onGloballyPositioned
    val current = coordinates.size

    val leftAnchor = maxOf(anchor.left, viewPadding.toInt())
    val rightAnchor = minOf(anchor.right, parent.width - viewPadding.toInt())

    val x = when {
        leftAnchor + current.width + viewPadding < parent.width -> {
            // Align left
            anchor.left - viewPadding.toInt()
        }

        rightAnchor - current.width - viewPadding >= 0 -> {
            // Align right
            rightAnchor - current.width - viewPadding.toInt()
        }

        else -> {
            // No horizontal alignment
            0
        }
    }

    val topAnchor = anchor.bottom + verticalSpacing
    val bottomAnchor = anchor.top - verticalSpacing

    val y = when {
        topAnchor + current.height + viewPadding < parent.height -> {
            // Display at bottom
            (topAnchor - viewPadding).toInt()
        }

        bottomAnchor - current.height - viewPadding >= 0 -> {
            // Display at top
            (bottomAnchor - current.height - viewPadding).toInt()
        }

        else -> {
            // Display middle vertically
            val middleAnchor = (parent.height - current.height) / 2
            (middleAnchor - viewPadding).toInt()
        }
    }

    offset.value = IntOffset(x, y)
}

/**
 * A MultiPreview annotation for desplaying a @[Composable] method using light and dark themes.
 *
 * Note that the app theme should support dark and light modes for these previews to be different.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION
)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class PreviewThemes
