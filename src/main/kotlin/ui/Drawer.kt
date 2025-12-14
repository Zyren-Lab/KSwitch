
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.io.File

/**
 * Right-side drawer content
 */
@Composable
fun DrawerContent(
    onBackupManagerClick: () -> Unit,
    onDonateClick: () -> Unit,
    onAboutClick: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(AppColors.BackgroundWhite)
            .padding(16.dp)
    ) {
        // Header
        Text("Menu", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
        Spacer(Modifier.height(24.dp))
        
        // Backup Manager
        DrawerItem(
            icon = Icons.Default.Folder,
            title = "Backup Manager",
            subtitle = "View and manage backups",
            onClick = {
                onBackupManagerClick()
                onCloseDrawer()
            }
        )
        
        Divider(modifier = Modifier.padding(vertical = 12.dp))
        
        // Donate
        DrawerItem(
            icon = Icons.Default.Favorite,
            title = "Donate",
            subtitle = "Support development",
            onClick = {
                onDonateClick()
                onCloseDrawer()
            }
        )
        
        // About
        DrawerItem(
            icon = Icons.Default.Info,
            title = "About",
            subtitle = "App information",
            onClick = {
                onAboutClick()
                onCloseDrawer()
            }
        )
        
        Spacer(Modifier.weight(1f))
        
        Text(
            "KSwitch v1.0.0",
            fontSize = 12.sp,
            color = AppColors.TextGray
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    ) {
        Icon(icon, title, tint = AppColors.SamsungBlue, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
            Text(subtitle, fontSize = 12.sp, color = AppColors.TextGray)
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
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        // Header with back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = AppColors.TextDark)
            }
            Text("Backup Manager", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Backup info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Folder, "Backup", tint = AppColors.SamsungBlue, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Backup Location", fontSize = 14.sp, color = AppColors.TextGray)
                        Text("./backup/", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = AppColors.TextDark)
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                Divider()
                Spacer(Modifier.height(20.dp))
                
                // Size
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Total Size", fontSize = 14.sp, color = AppColors.TextGray)
                        Text(backupInfo.totalSize, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Files", fontSize = 14.sp, color = AppColors.TextGray)
                        Text("${backupInfo.fileCount}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
                    }
                }
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        // Backup contents
        Text("Contents", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
        Spacer(Modifier.height(12.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(backupInfo.folders) { folder ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Folder, folder.name, tint = AppColors.TextGray)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(folder.name, fontSize = 15.sp, color = AppColors.TextDark)
                        Text("${folder.fileCount} files", fontSize = 12.sp, color = AppColors.TextGray)
                    }
                    Text(folder.size, fontSize = 14.sp, color = AppColors.TextGray)
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Delete button
        Button(
            onClick = { showDeleteConfirm = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.ErrorRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Default.Delete, "Delete", tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Delete All Backups", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Backups?") },
            text = { Text("This will permanently delete all backup data. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    backupDir.deleteRecursively()
                    backupInfo = getBackupInfo(backupDir)
                    showDeleteConfirm = false
                    onLog("All backups deleted")
                }) {
                    Text("DELETE", color = AppColors.ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

data class BackupFolderInfo(val name: String, val fileCount: Int, val size: String)
data class BackupInfo(val totalSize: String, val fileCount: Int, val folders: List<BackupFolderInfo>)

fun getBackupInfo(backupDir: File): BackupInfo {
    if (!backupDir.exists()) {
        return BackupInfo("0 B", 0, emptyList())
    }
    
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
        folders.add(BackupFolderInfo(folder.name, folderFiles, formatSize(folderSize)))
    }
    
    return BackupInfo(formatSize(totalBytes), totalFiles, folders)
}

fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.2f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
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
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    Icons.Default.PhoneAndroid,
                    "KSwitch",
                    tint = AppColors.SamsungBlue,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text("KSwitch", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
                Text("Version 1.0.0", fontSize = 14.sp, color = AppColors.TextGray)
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "A Samsung Smart Switch alternative for Linux.\n\n" +
                    "Backup and restore your Android device without root access.\n\n" +
                    "Features:\n" +
                    "• Media files (Images, Videos, Audio)\n" +
                    "• Documents and Archives\n" +
                    "• Installed Applications\n" +
                    "• Contacts & Call Logs\n\n" +
                    "Made with ❤️ using Kotlin & Compose",
                    fontSize = 14.sp,
                    color = AppColors.TextDark,
                    lineHeight = 22.sp
                )
                
                Spacer(Modifier.height(24.dp))
                
                Text(
                    "Click outside to close",
                    fontSize = 12.sp,
                    color = AppColors.TextGray
                )
            }
        }
    }
}
