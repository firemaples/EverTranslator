package tw.firemaples.onscreenocr.floatings.compose.base


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

lateinit var AppColorScheme: ColorScheme

@Composable
fun AppTheme(
    content: @Composable () -> Unit,
) {
    AppColorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content,
    )
}