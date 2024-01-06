package tw.firemaples.onscreenocr.floatings.compose.mainbar

import androidx.annotation.StringRes
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import tw.firemaples.onscreenocr.R

@Composable
fun MainBarMenu(
    expanded: Boolean,
    onMenuOptionSelected: (MainBarMenuOption?) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onMenuOptionSelected.invoke(null) },
    ) {
        MainBarMenuOption.entries.forEach { option ->
            DropdownMenuItem(
                text = {
                    Text(text = stringResource(id = option.text))
                },
                onClick = { onMenuOptionSelected.invoke(option) }
            )
        }
    }
}

enum class MainBarMenuOption(
    @StringRes
    val text: Int,
) {
    SETTING(R.string.menu_setting),
    PRIVACY_POLICY(R.string.menu_privacy_policy),
    ABOUT(R.string.menu_about),
    VERSION_HISTORY(R.string.menu_version_history),
    README(R.string.menu_readme),
    HIDE(R.string.menu_hide),
    EXIT(R.string.menu_exit),
}
