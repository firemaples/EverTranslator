package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.compose.base.AppColorScheme
import tw.firemaples.onscreenocr.floatings.compose.base.AppTheme
import tw.firemaples.onscreenocr.floatings.compose.base.FontSize
import tw.firemaples.onscreenocr.floatings.compose.base.pxToDp
import tw.firemaples.onscreenocr.floatings.compose.wigets.WordSelectionText
import tw.firemaples.onscreenocr.utils.UIUtils
import tw.firemaples.onscreenocr.utils.dpToPx
import java.util.Locale

@Composable
fun ResultViewContent(
    viewModel: ResultViewModel,
    requestRootLocationOnScreen: () -> Rect,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        val rootLocation = requestRootLocationOnScreen.invoke()
        viewModel.onRootViewPositioned(
            xOffset = rootLocation.left,
            yOffset = rootLocation.top,
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dialogOutside))
            .clickable(onClick = viewModel::onDialogOutsideClicked),
    ) {
        state.highlightArea.forEach {
            TextHighlightBox(
                highlightArea = it,
            )
        }

        val xOffset = remember { mutableStateOf(0) }
        val yOffset = remember { mutableStateOf(0) }

        ResultPanel(
            modifier = Modifier
                .calculateOffset(
                    requestRootLocationOnScreen = requestRootLocationOnScreen,
                    highlightUnion = state.highlightUnion,
                    xOffset = xOffset,
                    yOffset = yOffset,
                )
                .offset(
                    xOffset.value.pxToDp(),
                    yOffset.value.pxToDp(),
                ),
            viewModel = viewModel,
            textSearchEnabled = state.textSearchEnabled,
            fontSize = state.fontSize,
            ocrState = state.ocrState,
            translationState = state.translationState,
        )
    }
}

private fun Modifier.calculateOffset(
    requestRootLocationOnScreen: () -> Rect,
    highlightUnion: Rect,
    xOffset: MutableState<Int>,
    yOffset: MutableState<Int>,
): Modifier = onGloballyPositioned { coordinates ->
    val parentRect = requestRootLocationOnScreen.invoke()
    val anchorRect = Rect(highlightUnion).apply {
        top += parentRect.top
        left += parentRect.left
        bottom += parentRect.top
        right += parentRect.left
    }

    val bounds = coordinates.boundsInRoot()
    val left = parentRect.left + bounds.left
    val top = parentRect.top + bounds.top
    val right = parentRect.left + bounds.right
    val bottom = parentRect.top + bounds.bottom
    val windowRect = Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())

    val (leftMargin, topMargin) = UIUtils.countViewPosition(
        anchorRect, parentRect,
        windowRect.width(), windowRect.height(), 2.dpToPx(),
    )

    xOffset.value = leftMargin
    yOffset.value = topMargin
}

@Composable
private fun TextHighlightBox(highlightArea: Rect) {
    Box(
        modifier = Modifier
            .absoluteOffset(
                x = highlightArea.left.pxToDp(),
                y = highlightArea.top.pxToDp(),
            )
            .size(
                width = highlightArea
                    .width()
                    .pxToDp(),
                height = highlightArea
                    .height()
                    .pxToDp(),
            )
            .background(colorResource(id = R.color.resultView_recognizedBoundingBoxes))
    )
}

@Composable
private fun ResultPanel(
    modifier: Modifier,
    viewModel: ResultViewModel,
    textSearchEnabled: Boolean,
    fontSize: Float,
    ocrState: OCRState,
    translationState: TranslationState,
) {
    Column(
        modifier = modifier
            .widthIn(max = 300.dp)
            .background(
                color = AppColorScheme.background,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        OCRToolBar(
            textSearchEnabled = textSearchEnabled,
            onSearchClicked = viewModel::onTextSearchClicked,
            onEditClicked = viewModel::onOCRTextEditClicked,
            onCopyClicked = { viewModel.onCopyClicked(TextType.OCRText) },
            onFontSizeClicked = viewModel::onAdjustFontSizeClicked,
            onGoogleTranslateClicked = { viewModel.onGoogleTranslateClicked(TextType.OCRText) },
            onExportClicked = viewModel::onShareOCRTextClicked,
        )
        OCRTextArea(
            fontSize = fontSize,
            showProcessing = ocrState.showProcessing,
            ocrText = ocrState.ocrText,
            textSearchEnabled = textSearchEnabled,
            onTextSelected = viewModel::onTextSearchWordSelected,
        )

        if (translationState.showTranslationArea) {
            TranslationToolBar(
                onCopyClicked = { viewModel.onCopyClicked(TextType.TranslationResult) },
                onGoogleTranslateClicked = { viewModel.onGoogleTranslateClicked(TextType.TranslationResult) }
            )
            TranslationTextArea(
                fontSize = fontSize,
                showProcessing = translationState.showProcessing,
                translatedText = translationState.translatedText,
            )
            TranslationProviderBar(
                translationProviderText = translationState.providerText,
                translationProviderIcon = translationState.providerIcon,
            )
        }
    }
}

@Composable
private fun OCRToolBar(
    textSearchEnabled: Boolean,
    onSearchClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onCopyClicked: () -> Unit,
    onFontSizeClicked: () -> Unit,
    onGoogleTranslateClicked: () -> Unit,
    onExportClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.text_ocr_text),
            fontSize = FontSize.Small,
            fontWeight = FontWeight.Bold,
            color = AppColorScheme.onBackground,
        )

//            Image(painter = painterResource(id = R.drawable.ic_play), contentDescription = "")

        Spacer(modifier = Modifier.size(4.dp))

        val textSearchTintColor = if (textSearchEnabled)
            colorResource(id = R.color.md_blue_800)
        else AppColorScheme.onBackground
        Image(
            modifier = Modifier.clickable(onClick = onSearchClicked),
            painter = painterResource(id = R.drawable.ic_text_search),
            contentDescription = "",
            colorFilter = ColorFilter.tint(textSearchTintColor),
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onEditClicked),
            painter = painterResource(id = R.drawable.ic_square_edit_outline),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onCopyClicked),
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onFontSizeClicked),
            painter = painterResource(id = R.drawable.ic_font_size),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onGoogleTranslateClicked),
            painter = painterResource(id = R.drawable.ic_google_translate),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onExportClicked),
            painter = painterResource(id = R.drawable.ic_export),
            contentDescription = "",
        )
    }
}

@Composable
private fun OCRTextArea(
    showProcessing: Boolean,
    ocrText: String,
    fontSize: Float,
    textSearchEnabled: Boolean,
    onTextSelected: (String) -> Unit,
) {
    if (showProcessing) {
        ProgressIndicator()
    } else {
        if (textSearchEnabled) {
            WordSelectionText(
                modifier = Modifier
                    .sizeIn(maxHeight = 150.dp)
                    .verticalScroll(rememberScrollState()),
                text = ocrText,
                locale = Locale.US,
                textStyle = TextStyle(
                    color = AppColorScheme.onBackground,
                    fontSize = fontSize.sp,
                ),
                selectedSpanStyle = SpanStyle(
                    color = AppColorScheme.onSecondary,
                    background = AppColorScheme.secondary,
                ),
                onTextSelected = onTextSelected,
            )
        } else {
            Text(
                modifier = Modifier
                    .sizeIn(maxHeight = 150.dp)
                    .verticalScroll(rememberScrollState()),
                text = ocrText,
                color = AppColorScheme.onBackground,
                fontSize = fontSize.sp,
            )
        }
    }

}

@Composable
private fun TranslationToolBar(
    onCopyClicked: () -> Unit,
    onGoogleTranslateClicked: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.text_translated_text),
            fontSize = FontSize.Small,
            fontWeight = FontWeight.Bold,
            color = AppColorScheme.onBackground,
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onCopyClicked),
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            modifier = Modifier.clickable(onClick = onGoogleTranslateClicked),
            painter = painterResource(id = R.drawable.ic_google_translate),
            contentDescription = "",
        )
    }
}

@Composable
private fun TranslationTextArea(
    showProcessing: Boolean,
    translatedText: String,
    fontSize: Float,
) {

    if (showProcessing) {
        ProgressIndicator()
    } else {
        Text(
            modifier = Modifier
                .sizeIn(maxHeight = 150.dp)
                .verticalScroll(rememberScrollState()),
            text = translatedText,
            color = AppColorScheme.onBackground,
            fontSize = fontSize.sp,
        )
    }

}

@Composable
private fun ColumnScope.TranslationProviderBar(
    translationProviderText: String?,
    translationProviderIcon: Int?
) {
    if (translationProviderText != null || translationProviderIcon != null) {
        Spacer(modifier = Modifier.size(2.dp))
    }

    if (translationProviderText != null) {
        Text(
            modifier = Modifier.align(Alignment.End),
            text = translationProviderText,
            color = AppColorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }

    if (translationProviderIcon != null) {
        Image(
            modifier = Modifier.align(Alignment.End),
            painter = painterResource(id = translationProviderIcon),
            contentDescription = "",
        )
    }
}

@Composable
private fun ProgressIndicator() {
    CircularProgressIndicator(
        modifier = Modifier.size(30.dp),
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ResultViewContentPreview() {
    val areaRect = Rect(10, 20, 80, 90)
    val state = ResultViewState(
        highlightArea = listOf(areaRect),
        highlightUnion = areaRect,
        textSearchEnabled = true,
        ocrState = OCRState(
            showProcessing = true,
            ocrText = "Test OCR text",
        ),
        translationState = TranslationState(
            showTranslationArea = true,
            showProcessing = true,
            translatedText = "Test result text",
            providerText = "Test Translation Provider",
            providerIcon = R.drawable.img_translated_by_google,
        ),
    )

    val viewModel = object : ResultViewModel {
        override val state: StateFlow<ResultViewState>
            get() = MutableStateFlow(state)
        override val action: SharedFlow<ResultViewAction>
            get() = MutableSharedFlow()

        override fun onRootViewPositioned(xOffset: Int, yOffset: Int) = Unit
        override fun onDialogOutsideClicked() = Unit
        override fun onTextSearchClicked() = Unit
        override fun onTextSearchWordSelected(word: String) = Unit
        override fun onOCRTextEditClicked() = Unit
        override fun onOCRTextEdited(text: String) = Unit
        override fun onCopyClicked(textType: TextType) = Unit
        override fun onAdjustFontSizeClicked() = Unit
        override fun onGoogleTranslateClicked(textType: TextType) = Unit
        override fun onShareOCRTextClicked() = Unit
    }

    AppTheme {
        ResultViewContent(
            viewModel = viewModel,
            requestRootLocationOnScreen = { Rect() }
        )
    }
}
