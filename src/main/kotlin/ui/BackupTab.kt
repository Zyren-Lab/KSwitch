
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

        if (isTransferring) {
            Spacer(Modifier.weight(1f))
            Text("Backing Up Data...", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
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
            Text("Scanning Device", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
            Spacer(Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = scanProgress,
                modifier = Modifier.fillMaxWidth(0.6f).height(8.dp),
                color = AppColors.SamsungBlue
            )
            Spacer(Modifier.height(12.dp))
            Text("${(scanProgress * 100).toInt()}%", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
            Spacer(Modifier.height(8.dp))
            Text(scanStatus, color = AppColors.TextGray, fontSize = 14.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onCancelScan,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(150.dp).height(45.dp)
            ) {
                Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
            return
        }

        if (categoryData.isEmpty()) {
            Spacer(Modifier.weight(1f))
            Text("Scan your device to begin", fontSize = 18.sp, color = AppColors.TextDark)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.SamsungBlue),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(200.dp).height(50.dp)
            ) {
                Text("SCAN DEVICE", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.weight(1f))
        } else {
            Text("Select items to backup:", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextDark)
            Spacer(Modifier.height(16.dp))
            
            // Scrollable category list
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(0.85f)
                    .verticalScroll(rememberScrollState())
            ) {
                // ALL FILES option (special)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    elevation = 2.dp,
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = if (allFilesSelected) AppColors.SamsungBlue.copy(alpha = 0.1f) else Color.White
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Checkbox(
                            checked = allFilesSelected,
                            onCheckedChange = { onAllFilesToggle(it) },
                            colors = CheckboxDefaults.colors(checkedColor = AppColors.SamsungBlue),
                            enabled = scanComplete
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.SelectAll, "All Files", tint = AppColors.SamsungBlue)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("All Files", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextDark)
                            Text("Entire storage (except Android/data)", fontSize = 12.sp, color = AppColors.TextGray)
                        }
                    }
                }
                
                Divider()
                Spacer(Modifier.height(12.dp))
                
                // File categories
                Text("📁 Files", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextGray)
                categoryData.filter { !it.isSystemData && it.count > 0 }.forEach { data ->
                    CategoryCheckbox(data, data.category in selectedCategories, scanComplete && !allFilesSelected, onCategoryToggle)
                }
                
                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
                
                // System data categories
                Text("📱 System Data", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextGray)
                categoryData.filter { it.isSystemData && it.count > 0 }.forEach { data ->
                    CategoryCheckbox(data, data.category in selectedCategories, scanComplete, onCategoryToggle)
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onBackupClick,
                colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.SamsungBlue),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(200.dp).height(50.dp),
                enabled = scanComplete && (selectedCategories.isNotEmpty() || allFilesSelected)
            ) {
                Text("BACKUP NOW", color = Color.White, fontWeight = FontWeight.Bold)
            }
            
            if (!scanComplete) {
                Spacer(Modifier.height(8.dp))
                Text("Scan must complete 100%", color = Color.Red, fontSize = 12.sp)
            }
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
            onCheckedChange = { onToggle(data.category, it) },
            colors = CheckboxDefaults.colors(checkedColor = AppColors.SamsungBlue),
            enabled = enabled
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${data.displayName} (${CategoryManager.formatCount(data.count)})",
            fontSize = 15.sp,
            color = if (enabled) AppColors.TextDark else AppColors.TextGray
        )
        if (data.experimental) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Warning, "Experimental", tint = AppColors.WarningOrange, modifier = Modifier.size(16.dp))
        }
    }
}
