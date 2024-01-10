package tw.firemaples.onscreenocr.floatings.compose.resultview

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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

@Composable
fun ResultViewContent(
    resultViewModel: ResultViewModel,
) {
    val state by resultViewModel.state.collectAsState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.dialogOutside))
            .clickable(onClick = resultViewModel::onDialogOutsideClicked),
    ) {
        state.highlightArea?.let {
            TextHighlightBox(
                highlightArea = it,
            )
        }
        ResultPanel(
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
            .background(colorResource(id = R.color.resultView_recognizedBoundingBoxes))
    )
}

@Composable
private fun ResultPanel(
    ocrState: OCRState,
    translationState: TranslationState,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 300.dp)
            .background(AppColorScheme.background)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        OCRToolBar(
            textSearchEnabled = ocrState.textSearchEnabled,
        )
        OCRTextArea(
            showProcessing = ocrState.showProcessing,
            ocrText = ocrState.ocrText,
        )

        if (translationState.showTranslationArea) {
            TranslationToolBar()
            TranslationTextArea(
                showProcessing = translationState.showProcessing,
                translatedText = translationState.translatedText,
            )
            TranslationProviderBar(
                translationProviderText = translationState.translationProviderText,
                translationProviderIcon = translationState.translationProviderIcon,
            )
        }
    }
}

@Composable
private fun OCRToolBar(textSearchEnabled: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.text_ocr_text),
            fontSize = FontSize.Small,
            fontWeight = FontWeight.Bold,
        )

//            Image(painter = painterResource(id = R.drawable.ic_play), contentDescription = "")

        Spacer(modifier = Modifier.size(4.dp))

        val textSearchTintColor = if (textSearchEnabled)
            colorResource(id = R.color.md_blue_800)
        else AppColorScheme.onBackground
        Image(
            painter = painterResource(id = R.drawable.ic_text_search),
            contentDescription = "",
            colorFilter = ColorFilter.tint(textSearchTintColor),
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_square_edit_outline),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_font_size),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_google_translate),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_export),
            contentDescription = "",
        )
    }
}

@Composable
private fun OCRTextArea(
    showProcessing: Boolean,
    ocrText: String?,
) {
    if (showProcessing) {
        CircularProgressIndicator()
    }

    if (ocrText != null) {
        //TODO implement text search selector
        //TODO implement text overflow
        Text(text = ocrText)
    }
}

@Composable
private fun TranslationToolBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.text_translated_text),
            fontSize = FontSize.Small,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = "",
        )

        Spacer(modifier = Modifier.size(4.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_google_translate),
            contentDescription = "",
        )
    }
}

@Composable
private fun TranslationTextArea(
    showProcessing: Boolean,
    translatedText: String?,
) {

    if (showProcessing) {
        CircularProgressIndicator()
    }

    if (translatedText != null) {
        //TODO implement text overflow
        Text(text = translatedText)
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

@Preview(showBackground = true)
@Composable
private fun ResultViewContentPreview() {
    val state = ResultViewState(
        highlightArea = Rect(10, 20, 80, 90),
        ocrState = OCRState(
            showProcessing = true,
            ocrText = "Test OCR text",
            textSearchEnabled = true,
        ),
        translationState = TranslationState(
            showTranslationArea = true,
            showProcessing = true,
            translatedText = "Test result text",
            translationProviderText = "Test Translation Provider",
            translationProviderIcon = R.drawable.img_translated_by_google,
        ),
    )

    val viewModel = object : ResultViewModel {
        override val state: StateFlow<ResultViewState>
            get() = MutableStateFlow(state)
        override val action: SharedFlow<ResultViewAction>
            get() = MutableSharedFlow()

        override fun onDialogOutsideClicked() = Unit
    }

    AppTheme {
        ResultViewContent(
            resultViewModel = viewModel,
        )
    }
}
