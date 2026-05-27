/*
 * KSwitch - The GUI Backup Tool for Linux
 * Copyright (C) 2024-2025 ZyrenLab
 * ...
 */

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

@Composable
@Preview
fun App() {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    AppColors.isDark = isSystemDark
    
    MaterialTheme(
        colors = if (isSystemDark) darkColors(
            primary = AppColors.Primary,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onPrimary = Color.White,
            onBackground = AppColors.TextPrimary,
            onSurface = AppColors.TextPrimary
        ) else lightColors(
            primary = AppColors.Primary,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onPrimary = Color.White,
            onBackground = AppColors.TextPrimary,
            onSurface = AppColors.TextPrimary
        )
    ) { MainScreen() }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = AppConfig.APP_NAME,
        icon = painterResource("icon.png"),
        state = rememberWindowState(width = 1150.dp, height = 850.dp)
    ) { App() }
}