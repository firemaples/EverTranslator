package tw.firemaples.onscreenocr.floatings.compose.resultview

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatings.compose.base.calculateOffset
import tw.firemaples.onscreenocr.floatings.compose.base.clickableWithoutRipple
import tw.firemaples.onscreenocr.floatings.compose.base.dpToPx
import tw.firemaples.onscreenocr.floatings.compose.base.pxToDp
import tw.firemaples.onscreenocr.floatings.compose.wigets.WordSelectionText
import tw.firemaples.onscreenocr.theme.AppTheme
import tw.firemaples.onscreenocr.theme.FontSize
import java.util.Locale

@Composable
fun ResultViewContent(
    viewModel: ResultViewModel,
    requestRootLocationOnScreen: () -> Rect,
) {
    val state by viewModel.state.collectAsState()
    val emptyInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(Unit) {
        val rootLocation = requestRootLocationOnScreen.invoke()
        viewModel.onRootViewPositioned(
            xOffset = rootLocation.left,
            yOffset = rootLocation.top,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dialogOutside))
            .clickableWithoutRipple(
                interactionSource = emptyInteractionSource,
                onClick = viewModel::onDialogOutsideClicked,
            ),
    ) {
        state.highlightArea.forEach {
            TextHighlightBox(
                highlightArea = it,
            )
        }

        val targetOffset = remember {
            mutableStateOf(IntOffset(state.highlightUnion.left, state.highlightUnion.top))
        }

        val animOffset by animateIntOffsetAsState(
            targetValue = targetOffset.value,
            label = "result panel position",
        )

        val panelPadding = 16.dp

        ResultPanel(
            modifier = Modifier
                .padding(panelPadding)
                .run {
                    if (state.limitMaxWidth)
                        widthIn(max = 300.dp)
                    else this
                }
                .calculateOffset(
                    anchor = state.highlightUnion,
                    offset = targetOffset,
                    viewPadding = panelPadding.dpToPx(),
                    verticalSpacing = 4.dp.dpToPx(),
                )
                .offset { animOffset }
                .animateContentSize(),
            viewModel = viewModel,
            textSearchEnabled = state.textSearchEnabled,
            fontSize = state.fontSize,
            ocrState = state.ocrState,
            translationState = state.translationState,
        )
    }
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
            .background(
                color = colorResource(id = R.color.resultView_recognizedBoundingBoxes),
                shape = RoundedCornerShape(2.dp),
            )
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
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { }
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        if (ocrState.showRecognitionArea) {
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
        }

        if (translationState.showTranslationArea) {
            Spacer(modifier = Modifier.size(2.dp))
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
            color = MaterialTheme.colorScheme.onSurface,
        )

//            Image(painter = painterResource(id = R.drawable.ic_play), contentDescription = "")

        Spacer(modifier = Modifier.size(4.dp))

        val textSearchTintColor = if (textSearchEnabled)
            MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurface
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
                textStyle = LocalTextStyle.current.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = fontSize.sp,
                ),
                selectedSpanStyle = SpanStyle(
                    color = MaterialTheme.colorScheme.onSecondary,
                    background = MaterialTheme.colorScheme.secondary,
                ),
                onTextSelected = onTextSelected,
            )
        } else {
            Text(
                modifier = Modifier
                    .sizeIn(maxHeight = 150.dp)
                    .verticalScroll(rememberScrollState()),
                text = ocrText,
                color = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurface,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            maxLines = 1,
        )
    }

    if (translationProviderIcon != null) {
        Image(
            modifier = Modifier
                .align(Alignment.End)
                .height(16.dp),
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
        limitMaxWidth = true,
        textSearchEnabled = true,
        ocrState = OCRState(
            showProcessing = false,
            ocrText = "Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text Test OCR text ",
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
        override fun onHomeButtonPressed() = Unit
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
