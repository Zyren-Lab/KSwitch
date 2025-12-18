/*
 * KSwitch - The GUI Backup Tool for Linux
 * Copyright (C) 2024-2025 ZyrenLab
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

package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyStateScreen(onScanClick: () -> Unit) {
    Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Illustration placeholder
        Box(
                modifier =
                        Modifier.size(120.dp)
                                .background(
                                        MaterialTheme.colors.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(32.dp)
                                ),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = Icons.Default.Usb,
                    contentDescription = "USB Connection",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colors.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
                text = "Connect your Device",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
                text =
                        "Plug in your Android device via USB to start managing your data.\nMake sure USB Debugging is enabled.",
                fontSize = 16.sp,
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(56.dp).width(200.dp),
                colors =
                        ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.primary,
                                contentColor = Color.White
                        ),
                elevation =
                        ButtonDefaults.elevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 2.dp,
                                hoveredElevation = 12.dp
                        )
        ) { Text(text = "Scan for Devices", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
    }
}
