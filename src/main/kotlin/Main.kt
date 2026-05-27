/*
 * KSwitch - The GUI Backup Tool for Linux
 * Copyright (C) 2025-2026 Zyren-Lab
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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