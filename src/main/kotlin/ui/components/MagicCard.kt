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

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MagicCard(
        title: String,
        description: String, // Qo'shimcha ma'lumot
        icon: ImageVector, // Ikonka
        color: Color,
        onClick: () -> Unit
) {
        val interactionSource = remember { MutableInteractionSource() }
        val isHovered by interactionSource.collectIsHoveredAsState()

        // Sichqoncha borganda sal kattalashadi (Zoom effect)
        val scale by
                animateFloatAsState(
                        targetValue = if (isHovered) 1.02f else 1f,
                        animationSpec = tween(durationMillis = 200)
                )

        // Sichqoncha borganda soyasi qalinlashadi
        val elevation by
                animateDpAsState(
                        targetValue = if (isHovered) 12.dp else 4.dp,
                        animationSpec = tween(durationMillis = 200)
                )

        Card(
                modifier =
                        Modifier.size(
                                        width = 280.dp,
                                        height = 220.dp
                                ) // Karta o'lchami kattalashtirildi
                                .scale(scale)
                                .clickable(
                                        interactionSource = interactionSource,
                                        indication = null,
                                        onClick = onClick
                                ),
                shape = RoundedCornerShape(24.dp),
                elevation = 0.dp,
                backgroundColor =
                        if (isHovered) MaterialTheme.colors.surface.copy(alpha = 0.1f)
                        else Color.Transparent,
                border = if (isHovered) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
        ) {
                Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                        // 1. Rangli dumaloq ichidagi Ikonka
                        Box(
                                modifier =
                                        Modifier.size(70.dp)
                                                .background(color.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                        ) {
                                Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = color // Ikonka rangi
                                )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 2. Sarlavha (Backup/Restore)
                        Text(
                                text = title,
                                style = MaterialTheme.typography.h5,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Tushuntirish (Description)
                        Text(
                                text = description,
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp
                        )
                }
        }
}
