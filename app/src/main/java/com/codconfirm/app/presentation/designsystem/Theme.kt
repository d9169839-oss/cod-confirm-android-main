package com.codconfirm.app.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CodConfirmColors = lightColorScheme(
    primary = Color(0xFF176B5B),
    onPrimary = Color.White,
    secondary = Color(0xFFB76E3D),
    background = Color(0xFFF7F8F6),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF18211F),
)

@Composable
fun CodConfirmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CodConfirmColors,
        content = content,
    )
}
