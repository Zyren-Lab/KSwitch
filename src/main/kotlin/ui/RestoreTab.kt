
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.io.File

data class RestoreCategory(
    val name: String,
    val displayName: String,
    val itemCount: Int,
    val totalSize: String,
    val items: List<ManifestItem>
)

@Composable
fun RestoreTab(
    isConnected: Boolean,
    isRestoring: Boolean,
    progress: Float,
    progressText: String,
    backupDir: File,
    onRestoreClick: (selectedCategories: Set<String>) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isScanning by remember { mutableStateOf(false) }
    var scanComplete by remember { mutableStateOf(false) }
    var categories by remember { mutableStateOf(emptyList<RestoreCategory>()) }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var backupTimestamp by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("") }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isConnected) {
            Spacer(Modifier.weight(1f))
            Text("Connect your Android device via USB", fontSize = 18.sp, color = AppColors.TextGray)
            Spacer(Modifier.weight(1f))
            return
        }
        
        if (isRestoring) {
            Spacer(Modifier.weight(1f))
            Text("Restoring Data...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = AppColors.SamsungBlue
            )
            Spacer(Modifier.height(8.dp))
            Text(progressText, color = AppColors.TextGray)
            Spacer(Modifier.weight(1f))
            return
        }
        
        if (isScanning) {
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(color = AppColors.SamsungBlue)
            Spacer(Modifier.height(16.dp))
            Text("Scanning backup folder...", color = AppColors.TextGray)
            Spacer(Modifier.weight(1f))
            return
        }
        
        if (!scanComplete) {
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.Folder, "Backup", tint = AppColors.SamsungBlue, modifier = Modifier.size(64.dp))
            Spacer(Modifier.height(16.dp))
            Text("Restore from backup", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
            Spacer(Modifier.height(8.dp))
            Text("Scan your backup folder to see available items", color = AppColors.TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    isScanning = true
                    scope.launch {
                        // Try to read manifest first, then scan folder
                        val manifest = ManifestManager.readManifest(backupDir)
                            ?: ManifestManager.scanBackupFolder(backupDir)
                        
                        backupTimestamp = manifest.timestamp
                        deviceName = manifest.deviceName
                        
                        categories = manifest.categories.map { (name, items) ->
                            val displayName = when (name) {
                                "InstalledApps" -> "Installed Apps"
                                "CallLogs" -> "Call Logs"
                                else -> name
                            }
                            val totalSize = items.sumOf { it.size }
                            RestoreCategory(
                                name = name,
                                displayName = displayName,
                                itemCount = items.size,
                                totalSize = formatSize(totalSize),
                                items = items
                            )
                        }.filter { it.itemCount > 0 }
                        
                        // Auto-select all
                        selectedCategories = categories.map { it.name }.toSet()
                        
                        isScanning = false
                        scanComplete = true
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.SamsungBlue),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(200.dp).height(50.dp)
            ) {
                Text("SCAN BACKUP", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
        } else {
            // Show backup info
            if (backupTimestamp.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f).padding(bottom = 16.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Backup Info", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextGray)
                        if (deviceName.isNotEmpty()) {
                            Text("Device: $deviceName", fontSize = 13.sp, color = AppColors.TextDark)
                        }
                        Text("Date: $backupTimestamp", fontSize = 13.sp, color = AppColors.TextDark)
                    }
                }
            }
            
            Text("Select items to restore:", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
            Spacer(Modifier.height(12.dp))
            
            if (categories.isEmpty()) {
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Warning, "Empty", tint = AppColors.WarningOrange, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No backup data found", color = AppColors.TextGray)
                Spacer(Modifier.weight(1f))
            } else {
                // Category list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.85f)
                        .verticalScroll(rememberScrollState())
                ) {
                    categories.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Checkbox(
                                checked = category.name in selectedCategories,
                                onCheckedChange = { checked ->
                                    selectedCategories = if (checked) {
                                        selectedCategories + category.name
                                    } else {
                                        selectedCategories - category.name
                                    }
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.SamsungBlue)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(category.displayName, fontSize = 15.sp, color = AppColors.TextDark)
                                Text(
                                    "${category.itemCount} items • ${category.totalSize}",
                                    fontSize = 12.sp,
                                    color = AppColors.TextGray
                                )
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(
                    onClick = { onRestoreClick(selectedCategories) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.SamsungBlue),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.width(200.dp).height(50.dp),
                    enabled = selectedCategories.isNotEmpty()
                ) {
                    Text("RESTORE NOW", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
