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
 
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ui.components.AppSelectionDialog

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
    installedApps: List<InstalledApp>, // <-- YANGI: Ilovalar ro'yxati kerak
    selectedPackageNames: Set<String>, // <-- YANGI: Tanlangan ilovalar
    onUpdateSelectedApps: (Set<String>) -> Unit, // <-- YANGI: Update qilish funksiyasi
    onScanClick: () -> Unit,
    onCancelScan: () -> Unit,
    onBackupClick: () -> Unit,
    onCategoryToggle: (Category, Boolean) -> Unit,
    onAllFilesToggle: (Boolean) -> Unit
) {
    var showAppDialog by remember { mutableStateOf(false) }


    // BackupTab ichida Dialog chaqiruvi
    if (showAppDialog) {
        AppSelectionDialog(
            apps = installedApps,
            // Agar birorta ham tanlanmagan bo'lsa (boshida), hammasini berib yuboramiz
            selectedPackages = if (selectedPackageNames.isEmpty()) installedApps.map { it.packageName }.toSet() else selectedPackageNames,
            onDismiss = { showAppDialog = false },
            onConfirm = { newSelection ->
                onUpdateSelectedApps(newSelection)
                showAppDialog = false
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // ... (Eski isConnected, isTransferring, isScanning qismlari o'zgarishsiz qoladi - nusxalab oling)
        // ...

        // ASOSIY RO'YXAT QISMI (O'zgargan joyi)
        if (!scanComplete) {
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(color = AppColors.Primary)
            Spacer(Modifier.weight(1f))
            return
        }

        Text("Select items to backup:", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        Spacer(Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.9f) // Biroz kengroq
                .verticalScroll(rememberScrollState())
        ) {
            // 1. ALL FILES (Master Card)
            ModernSelectionRow(
                title = "All Files",
                subtitle = "Entire storage (Recommended)",
                icon = Icons.Default.Folder,
                isChecked = allFilesSelected,
                onToggle = { onAllFilesToggle(!allFilesSelected) }, // Butun qator bosilganda ishlaydi
                isHeader = true
            )

            Spacer(Modifier.height(16.dp))

            // 2. FILES (Rasmlar, Videolar...)
            Text("FILES", style = MaterialTheme.typography.overline, color = AppColors.TextSecondary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))

            categoryData.filter { !it.isSystemData && it.count > 0 }.forEach { data ->
                ModernSelectionRow(
                    title = data.displayName,
                    subtitle = "${CategoryManager.formatCount(data.count)} items",
                    icon = getIconForCategory(data.category),
                    isChecked = if (allFilesSelected) true else data.category in selectedCategories,
                    isEnabled = !allFilesSelected,
                    onToggle = { onCategoryToggle(data.category, !selectedCategories.contains(data.category)) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // 3. APPS & DATA
            Text("SYSTEM & APPS", style = MaterialTheme.typography.overline, color = AppColors.TextSecondary, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp))

            categoryData.filter { it.isSystemData && it.count > 0 }.forEach { data ->
                val isAppCategory = data.category == Category.INSTALLED_APPS
                val isChecked = data.category in selectedCategories

                // Ilovalar uchun maxsus subtitle
                val subtitle = if (isAppCategory) {
                    if (isChecked) "${selectedPackageNames.size} apps selected" else "Select specific apps"
                } else {
                    "${CategoryManager.formatCount(data.count)} items"
                }

                ModernSelectionRow(
                    title = data.displayName,
                    subtitle = subtitle,
                    icon = getIconForCategory(data.category),
                    isChecked = isChecked,
                    onToggle = { onCategoryToggle(data.category, !isChecked) },
                    // Faqat Ilovalar uchun "Settings" tugmasini ko'rsatamiz
                    hasSettings = isAppCategory,
                    onSettingsClick = { showAppDialog = true }
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // BACKUP BUTTON
        Button(
            onClick = onBackupClick,
            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Primary),
            shape = RoundedCornerShape(50), // Smart Switch kabi dumaloq
            modifier = Modifier.width(220.dp).height(56.dp),
            enabled = selectedCategories.isNotEmpty() || allFilesSelected
        ) {
            Text("BACKUP NOW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ZAMONAVIY QATOR DIZAYNI (Siz xohlagandek)
@Composable
fun ModernSelectionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isChecked: Boolean,
    isEnabled: Boolean = true,
    isHeader: Boolean = false,
    hasSettings: Boolean = false,
    onToggle: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val backgroundColor = if (isHeader) AppColors.Primary.copy(alpha = 0.1f) else AppColors.Surface
    val contentColor = if (isEnabled) AppColors.TextPrimary else AppColors.TextDisabled

    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp,
        border = BorderStroke(1.dp, if (isChecked) AppColors.Primary.copy(alpha = 0.5f) else Color.Transparent),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled) { onToggle() } // BUTUN QATOR BOSILADI!
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isChecked) AppColors.Primary else Color.Gray.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(16.dp))

            // Texts
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = contentColor)
                Text(subtitle, fontSize = 12.sp, color = if (isEnabled) AppColors.TextSecondary else AppColors.TextDisabled)
            }

            // Settings Button (Faqat Apps uchun)
            if (hasSettings && isChecked) {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, "Select Apps", tint = AppColors.Primary)
                }
            }

            // Checkbox (Clickable emas, faqat ko'rinish uchun, chunki Row o'zi clickable)
            Checkbox(
                checked = isChecked,
                onCheckedChange = null, // null qo'yamiz, chunki Row boshqaradi
                colors = CheckboxDefaults.colors(
                    checkedColor = AppColors.Primary,
                    checkmarkColor = Color.White,
                    disabledColor = AppColors.TextDisabled
                ),
                enabled = isEnabled
            )
        }
    }
}

// Ikonkalar yordamchisi
fun getIconForCategory(category: Category): ImageVector {
    return when(category) {
        Category.IMAGES -> Icons.Default.Image
        Category.VIDEOS -> Icons.Default.Videocam
        Category.AUDIO -> Icons.Default.Audiotrack
        Category.DOCS -> Icons.Default.Description
        Category.ARCHIVES -> Icons.Default.Archive
        Category.INSTALLED_APPS -> Icons.Default.Apps
        Category.CONTACTS -> Icons.Default.Contacts
        Category.CALL_LOGS -> Icons.Default.Call
        else -> Icons.Default.Folder
    }
}
