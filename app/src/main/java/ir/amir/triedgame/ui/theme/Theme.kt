package ir.amir.triedgame.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val TsBackground = Color(0xFF0E1117)
val TsSurface = Color(0xFF161B22)
val TsGreen = Color(0xFF16C784)
val TsRed = Color(0xFFEA3943)
val TsAccent = Color(0xFF4C8DFF)
val TsTextPrimary = Color(0xFFE6E9EF)
val TsTextSecondary = Color(0xFF8B93A1)

private val TraderShowColorScheme = darkColorScheme(
    primary = TsAccent,
    background = TsBackground,
    surface = TsSurface,
    onPrimary = TsTextPrimary,
    onBackground = TsTextPrimary,
    onSurface = TsTextPrimary,
    error = TsRed
)

@Composable
fun TraderShowTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TraderShowColorScheme,
        content = content
    )
}
