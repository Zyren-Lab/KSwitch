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
 
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BackupTab(
    isConnected: Boolean,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatus: String,
    scanComplete: Boolean,
    isTransferring: Boolean,
    categoryData: List<CategoryData>,
    selectedCategories: Set<Category>,
    allFilesSelected: Boolean,
    progress: Float,
    progressText: String,
    onScanClick: () -> Unit,
    onCancelScan: () -> Unit,
    onBackupClick: () -> Unit,
    onCategoryToggle: (Category, Boolean) -> Unit,
    onAllFilesToggle: (Boolean) -> Unit
) {
    // File categories that are controlled by "All Files"
    val fileCategories = listOf(Category.IMAGES, Category.VIDEOS, Category.AUDIO,
        Category.ARCHIVES, Category.DOCS, Category.OTHERS)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isConnected) {
            Spacer(Modifier.weight(1f))
            Text("Connect your Android device via USB", fontSize = 18.sp, color = AppColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            return
        }

        if (isTransferring) {
            Spacer(Modifier.weight(1f))
            Text("Backing Up Data...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = AppColors.Primary,
                backgroundColor = AppColors.SurfaceLight
            )
            Spacer(Modifier.height(8.dp))
            Text(progressText, color = AppColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            return
        }

        if (isScanning) {
            Spacer(Modifier.weight(1f))
            Text("Scanning Device", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = scanProgress,
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = AppColors.Primary,
                backgroundColor = AppColors.SurfaceLight
            )
            Spacer(Modifier.height(12.dp))
            Text("${(scanProgress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text(scanStatus, color = AppColors.TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCancelScan,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Error),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(150.dp).height(45.dp)
            ) {
                Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            return
        }
        if (!scanComplete) {
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(color = AppColors.Primary)
            Spacer(Modifier.weight(1f))
            return
        }

        Text("Select items to backup:", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.85f)
                .verticalScroll(rememberScrollState())
        ) {
            // ALL FILES - Master checkbox
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp),
                backgroundColor = if (allFilesSelected) AppColors.Primary.copy(alpha = 0.2f) else AppColors.Surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Checkbox(
                        checked = allFilesSelected,
                        onCheckedChange = { onAllFilesToggle(it) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColors.Primary,
                            uncheckedColor = AppColors.TextSecondary
                        ),
                        enabled = scanComplete
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.SelectAll, "All Files", tint = AppColors.Accent)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("All Files (recommended)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                        Text("Entire storage (except Android/data)", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }
            }

            Divider(color = AppColors.Divider)
            Spacer(Modifier.height(12.dp))

            // File categories - disabled when All Files is checked
            Text("📁 Files", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextSecondary)
            categoryData.filter { !it.isSystemData && it.count > 0 }.forEach { data ->
                val isChecked = if (allFilesSelected) true else data.category in selectedCategories
                val isEnabled = scanComplete && !allFilesSelected

                CategoryCheckbox(
                    data = data,
                    isChecked = isChecked,
                    enabled = isEnabled,
                    onToggle = onCategoryToggle
                )
            }

            Spacer(Modifier.height(16.dp))
            Divider(color = AppColors.Divider)
            Spacer(Modifier.height(16.dp))

            // System data - always independent
            Text("📱 System Data", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextSecondary)
            categoryData.filter { it.isSystemData && it.count > 0 }.forEach { data ->
                CategoryCheckbox(
                    data = data,
                    isChecked = data.category in selectedCategories,
                    enabled = scanComplete,
                    onToggle = onCategoryToggle
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onBackupClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Primary),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.width(200.dp).height(50.dp),
            enabled = scanComplete && (selectedCategories.isNotEmpty() || allFilesSelected)
        ) {
            Text("BACKUP NOW", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (!scanComplete) {
            Spacer(Modifier.height(8.dp))
            Text("Scan must complete 100%", color = AppColors.Error, fontSize = 12.sp)
        }
    }
}

@Composable
fun CategoryCheckbox(
    data: CategoryData,
    isChecked: Boolean,
    enabled: Boolean,
    onToggle: (Category, Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { if (enabled) onToggle(data.category, it) },
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.Primary,
                uncheckedColor = AppColors.TextSecondary,
                disabledColor = if (isChecked) AppColors.Primary.copy(alpha = 0.6f) else AppColors.TextDisabled,
                checkmarkColor = Color.White
            ),
            enabled = enabled
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${data.displayName} (${CategoryManager.formatCount(data.count)})",
            fontSize = 15.sp,
            // Readable even when disabled if checked
            color = when {
                enabled -> AppColors.TextPrimary
                isChecked -> AppColors.TextDisabledReadable
                else -> AppColors.TextDisabled
            }
        )
        if (data.experimental) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Warning, "Experimental", tint = AppColors.Warning, modifier = Modifier.size(16.dp))
        }
    }
}
