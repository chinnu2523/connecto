package com.example.connecto.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.connecto.ui.designsystem.ConnectoDesignTheme

@Composable
fun ConnectoTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ConnectoDesignTheme(
        darkTheme = darkTheme,
        content = content
    )
}
