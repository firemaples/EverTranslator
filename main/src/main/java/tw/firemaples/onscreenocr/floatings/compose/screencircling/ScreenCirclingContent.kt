package tw.firemaples.onscreenocr.floatings.compose.screencircling

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.MutableStateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.screenCircling.CirclingView
import tw.firemaples.onscreenocr.floatings.screenCircling.HelperTextView
import tw.firemaples.onscreenocr.floatings.screenCircling.ProgressBorderView
import tw.firemaples.onscreenocr.theme.AppTheme
import tw.firemaples.onscreenocr.utils.getViewRect
import tw.firemaples.onscreenocr.utils.onViewPrepared

@Composable
fun ScreenCirclingContent(viewModel: ScreenCirclingViewModel) {
    val state by viewModel.state.collectAsState()
    val helperTextView = remember { mutableStateOf<HelperTextView?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onViewDisplayed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dialogOutside))
    ) {
        ProgressBorderView()

        CirclingView(
            selectedArea = state.selectedArea,
            helperTextView = helperTextView,
            onViewPrepared = viewModel::onCirclingViewPrepared,
            onAreaSelected = viewModel::onAreaSelected,
        )

        HelperTextView(helperTextView = helperTextView)
    }
}

@Composable
private fun ProgressBorderView() {
    var run by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            run = false
        }
    }

    // TODO refactor to compose
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            ProgressBorderView(context = context).also { view ->
                if (run) {
                    view.start()
                }
            }
        },
        update = { view ->
            if (!run) {
                view.stop()
            }
        }
    )
}

@Composable
private fun CirclingView(
    selectedArea: Rect?,
    helperTextView: MutableState<HelperTextView?>,
    onViewPrepared: (viewRect: Rect) -> Unit,
    onAreaSelected: (selected: Rect) -> Unit,
) {
    // TODO refactor to compose
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            CirclingView(
                context = context,
            ).apply {
                this.selectedBox = selectedArea

                this.onAreaSelected = { selected ->
                    onAreaSelected.invoke(selected)
                }

                onViewPrepared {
                    onViewPrepared.invoke(getViewRect())
                }
            }
        },
        update = { view ->
            view.helperTextView = helperTextView.value
        },
    )
}

@Composable
private fun HelperTextView(helperTextView: MutableState<HelperTextView?>) {
    // TODO refactor to compose
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            HelperTextView(context = context).also { view ->
                helperTextView.value = view
            }
        },
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ScreenCircleContentPreview() {
    val viewModel = object : ScreenCirclingViewModel {
        override val state = MutableStateFlow(ScreenCirclingState())
        override fun onViewDisplayed() = Unit
        override fun onTranslateClicked() = Unit
        override fun onCloseClick() = Unit
        override fun onCirclingViewPrepared(viewRect: Rect) = Unit
        override fun onAreaSelected(selected: Rect) = Unit
    }

    AppTheme {
        ScreenCirclingContent(
            viewModel = viewModel,
        )
    }
}

//@Composable
//private fun CirclingView() {
//    var startPoint: Offset? by remember { mutableStateOf(null) }
//    var endPoint: Offset by remember { mutableStateOf(Offset(0f, 0f)) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .pointerInput(Unit) {
//                detectDragGestures(
//                    onDragStart = { offset: Offset ->
//                        composeDebug("onDragStart()")
//                        startPoint = offset
//                        endPoint = offset
//                    },
//                    onDragEnd = {},
//                    onDragCancel = {},
//                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
//                        endPoint += dragAmount
//                    }
//                )
////                detectTransformGestures(
////                    onGesture = { centroid: Offset, pan: Offset, zoom: Float, rotation: Float ->
////                        composeDebug("onGesture(), centroid: $centroid, pan: $pan, zoom: $zoom, rotation: $rotation")
////                        var start = startPoint
////                        if (start == null) {
////                            startPoint = centroid
////                            return@detectTransformGestures
////                        }
//////                        var start = startPoint ?: return@detectTransformGestures
////                        var end = centroid
////
////                        start += pan
////                        end += pan
////
////                        startPoint = start
////                        endPoint = end
////                    }
////                )
//            }
//            .drawBehind {
//                val start = startPoint ?: return@drawBehind
//                val end = endPoint
//                val left = min(start.x, end.x)
//                val top = min(start.y, end.y)
//                val width = abs(start.x - end.x)
//                val height = abs(start.y - end.y)
//
//                drawRect(
//                    color = Color.Green,
//                    topLeft = Offset(left, top),
//                    size = Size(width, height),
//                    style = Stroke(width = 2.dp.toPx()),
//                )
//            }
//    )
//}
