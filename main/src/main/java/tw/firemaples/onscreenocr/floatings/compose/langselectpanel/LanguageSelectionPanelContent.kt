package tw.firemaples.onscreenocr.floatings.compose.langselectpanel

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.theme.AppTheme

@Composable
fun LanguageSelectionPanelContent(viewModel: LanguageSelectionPanelViewModel) {
    val state by viewModel.state.collectAsState()

    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp, bottom = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .width(150.dp)
                .height(300.dp),
        ) {
            OCRLanguageListContent(
                ocrLanguageList = state.ocrLanguageList,
            )
        }

        Spacer(modifier = Modifier.size(4.dp))

        Column(
            modifier = Modifier
                .width(150.dp)
                .height(300.dp),
        ) {
            TranslationLanguageContent()
        }
    }
}

@Composable
private fun OCRLanguageListContent(
    ocrLanguageList: List<OCRLanguage>,
) {
    Title(text = R.string.text_ocr_language)

    Spacer(modifier = Modifier.size(2.dp))

    LazyColumn(

    ) {
        itemsIndexed(ocrLanguageList) { index, item ->
            LanguageItem(
                text = item.languageName,
                selected = item.selected,
                onClicked = {},
            )

            Divider()
        }
    }
}

@Composable
private fun TranslationLanguageContent() {
    Title(text = R.string.text_translation)
}

@Composable
private fun Title(@StringRes text: Int) {
    Text(
        text = stringResource(id = text),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
    )

    Divider(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        thickness = 1.dp,
    )
}

@Composable
private fun LanguageItem(
    text: String,
    selected: Boolean,
    onClicked: () -> Unit,
) {
    val background =
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surface
    val textColor =
        if (selected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurface

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = background)
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .clickable(onClick = onClicked),
        text = text,
        color = textColor,
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LanguageSelectionPanelContentPreview() {
    val ocrLanguageList = listOf(
        OCRLanguage(
            languageName = "English",
            selected = true,
        ),
        OCRLanguage(
            languageName = "Chinese",
            selected = false,
        ),
    )
    val state = LanguageSelectionPanelState(
        ocrLanguageList = ocrLanguageList,
    )
    val viewModel = object : LanguageSelectionPanelViewModel {
        override val state = MutableStateFlow(state)
    }

    AppTheme {
        LanguageSelectionPanelContent(viewModel = viewModel)
    }
}
