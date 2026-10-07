package com.example.connecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.connecto.ui.designsystem.ConnectoDesignTheme
import com.example.connecto.ui.designsystem.ThemeMode

@Composable
fun ConnectoTheme(
    themeMode: ThemeMode = ThemeMode.NOIR,
    darkTheme: Boolean = themeMode.isDark,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ConnectoDesignTheme(
        themeMode = themeMode,
        darkTheme = darkTheme,
        content = content
    )
}
