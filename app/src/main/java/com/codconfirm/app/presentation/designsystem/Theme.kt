package com.codconfirm.app.presentation.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CodConfirmLightColors = lightColorScheme(
    primary = Color(0xFF176B5B),
    onPrimary = Color.White,
    secondary = Color(0xFFB76E3D),
    background = Color(0xFFF7F8F6),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18211F),
)

private val CodConfirmDarkColors = darkColorScheme(
    primary = Color(0xFF79D5BE),
    onPrimary = Color(0xFF00382F),
    secondary = Color(0xFFE8B18A),
    background = Color(0xFF101816),
    surface = Color(0xFF18211F),
    onSurface = Color(0xFFE1E9E5),
)

@Composable
fun CodConfirmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) CodConfirmDarkColors else CodConfirmLightColors,
        content = content,
    )
}
