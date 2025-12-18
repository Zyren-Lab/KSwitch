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
 
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.border
import java.io.File

/**
 * Backup Success Dialog - The "Wow Factor"
 */
@Composable
fun BackupSuccessDialog(
    fileCount: Int,
    totalSize: String,
    backupPath: String,
    onOpenFolder: () -> Unit,
    onDonate: () -> Unit,
    onClose: () -> Unit
) {
    // Animated checkmark scale
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Dialog(onDismissRequest = onClose) {
        Card(
            modifier = Modifier.width(420.dp),
            elevation = 16.dp,
            shape = RoundedCornerShape(24.dp),
            backgroundColor = AppColors.Surface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                // Animated Success Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(AppColors.Success.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = AppColors.Success,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Backup Completed Successfully! 🎉",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AppColors.SurfaceLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, "Files", tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("$fileCount files saved", fontSize = 14.sp, color = AppColors.TextPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, "Size", tint = AppColors.Accent, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Total size: $totalSize", fontSize = 14.sp, color = AppColors.TextPrimary)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "📁 $backupPath",
                            fontSize = 12.sp,
                            color = AppColors.TextSecondary
                        )
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Donate Hook
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = AppColors.DonateGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Did KSwitch save your day?",
                            fontSize = 14.sp,
                            color = AppColors.TextPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onDonate,
                            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.DonateGold),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Coffee, "Donate", tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Buy me a coffee ☕", color = Color.Black, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onOpenFolder,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, "Open", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Folder")
                    }
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

/**
 * Modern Dropdown Menu for TopAppBar
 */
@Composable
fun TopBarMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onBackupManager: () -> Unit,
    onAbout: () -> Unit,
    onCheckUpdates: () -> Unit,
    onDonate: () -> Unit
) {
    MaterialTheme(
        shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .background(AppColors.Surface)
                .width(240.dp) 
                .border(
                    width = 1.dp,
                    color = AppColors.Divider.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Text(
                text = "Menu",
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
            )

            // ITEM 1: Backup Manager
            StyledMenuItem(
                icon = Icons.Default.Folder,
                text = "Backup Manager",
                iconColor = AppColors.Primary,
                onClick = {
                    onBackupManager()
                    onDismiss()
                }
            )

            Divider(
                color = AppColors.Divider.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ITEM 2: About
            StyledMenuItem(
                icon = Icons.Default.Info,
                text = "About KSwitch",
                onClick = {
                    onAbout()
                    onDismiss()
                }
            )

            // ITEM 3: Check Updates
            StyledMenuItem(
                icon = Icons.Default.Update,
                text = "Check for Updates",
                onClick = {
                    onCheckUpdates()
                    onDismiss()
                }
            )

            Divider(
                color = AppColors.Divider.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ITEM 4: Donate 
            DropdownMenuItem(
                onClick = {
                    onDonate()
                    onDismiss()
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Donate",
                    tint = Color(0xFFFF4081),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Support Development",
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Buy me a coffee ☕",
                        color = AppColors.TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StyledMenuItem(
    icon: ImageVector,
    text: String,
    iconColor: Color = AppColors.TextSecondary,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp) 
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = text,
            color = AppColors.TextPrimary,
            fontSize = 14.sp
        )
    }
}


/**
 * About Dialog
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.width(400.dp),
            elevation = 12.dp,
            shape = RoundedCornerShape(20.dp),
            backgroundColor = AppColors.Surface
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    "KSwitch",
                    tint = AppColors.Primary,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text("KSwitch", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                Text("Version ${AppConfig.VERSION}", fontSize = 14.sp, color = AppColors.TextSecondary)
                
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "Android backup & restore tool for Linux.\n\n" +
                    "• Full device backup without root\n" +
                    "• Installed Apps, Contacts, Call Logs\n" +
                    "• Media files & Documents\n\n" +
                    "Developed by Zyren Lab",
                    fontSize = 14.sp,
                    color = AppColors.TextPrimary,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close", color = Color.White)
                }
            }
        }
    }
}

/**
 * Backup Manager Screen
 */
@Composable
fun BackupManagerScreen(
    onBack: () -> Unit,
    onLog: (String) -> Unit
) {
    val backupDir = File("backup")
    var backupInfo by remember { mutableStateOf(getBackupInfo(backupDir)) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize().background(AppColors.Background).padding(24.dp)
    ) {
        // Header with back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = AppColors.TextPrimary)
            }
            Text("Backup Manager", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Backup info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp),
            backgroundColor = AppColors.Surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, "Backup", tint = AppColors.Primary, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Backup Location", fontSize = 14.sp, color = AppColors.TextSecondary)
                        Text("./backup/", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                Divider(color = AppColors.Divider)
                Spacer(Modifier.height(20.dp))
                
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Size", fontSize = 14.sp, color = AppColors.TextSecondary)
                        Text(backupInfo.totalSize, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Files", fontSize = 14.sp, color = AppColors.TextSecondary)
                        Text("${backupInfo.fileCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text("Contents", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
        Spacer(Modifier.height(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            backupInfo.folders.forEach { folder ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Folder, folder.name, tint = AppColors.TextSecondary)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(folder.name, fontSize = 15.sp, color = AppColors.TextPrimary)
                        Text("${folder.fileCount} files", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                    Text(folder.size, fontSize = 14.sp, color = AppColors.TextSecondary)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.Error),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Delete All Backups", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            backgroundColor = AppColors.Surface,
            title = { Text("Delete Backups?", color = AppColors.TextPrimary) },
            text = { Text("This will permanently delete all backup data.", color = AppColors.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    backupDir.deleteRecursively()
                    backupInfo = getBackupInfo(backupDir)
                    showDeleteConfirm = false
                    onLog("All backups deleted")
                }) {
                    Text("DELETE", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL", color = AppColors.TextSecondary)
                }
            }
        )
    }
}

data class BackupFolderInfo(val name: String, val fileCount: Int, val size: String)
data class BackupInfo(val totalSize: String, val fileCount: Int, val folders: List<BackupFolderInfo>)

fun getBackupInfo(backupDir: File): BackupInfo {
    if (!backupDir.exists()) return BackupInfo("0 B", 0, emptyList())
    
    var totalBytes = 0L
    var totalFiles = 0
    val folders = mutableListOf<BackupFolderInfo>()
    
    backupDir.listFiles()?.filter { it.isDirectory }?.forEach { folder ->
        var folderSize = 0L
        var folderFiles = 0
        folder.walk().filter { it.isFile }.forEach { file ->
            folderSize += file.length()
            folderFiles++
        }
        totalBytes += folderSize
        totalFiles += folderFiles
        folders.add(BackupFolderInfo(folder.name, folderFiles, formatSizeLocal(folderSize)))
    }
    
    return BackupInfo(formatSizeLocal(totalBytes), totalFiles, folders)
}

private fun formatSizeLocal(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.2f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
