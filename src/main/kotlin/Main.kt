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
 
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

@Composable
@Preview
fun App() {
    MaterialTheme(
        colors = darkColors(
            primary = AppColors.Primary,
            background = AppColors.Background,
            surface = AppColors.Surface,
            onPrimary = Color.White,
            onBackground = AppColors.TextPrimary,
            onSurface = AppColors.TextPrimary
        )
    ) {
        MainScreen()
    }
}

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf(emptyList<String>()) }
    var isConnected by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf(0) }
    var logs by remember { mutableStateOf(listOf<String>()) }
    
    // UI state
    var showMenu by remember { mutableStateOf(false) }
    var showBackupManager by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var backupStats by remember { mutableStateOf(BackupStats(0, "0 B", "")) }
    
    // Scan state
    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanStatus by remember { mutableStateOf("") }
    var scanJob by remember { mutableStateOf<Job?>(null) }
    var scanComplete by remember { mutableStateOf(false) }
    var categoryData by remember { mutableStateOf(emptyList<CategoryData>()) }
    var selectedCategories by remember { mutableStateOf(setOf<Category>()) }
    var allFilesSelected by remember { mutableStateOf(false) }
    
    // System data
    var installedApps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    
    // Transfer state
    var isTransferring by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var progressText by remember { mutableStateOf("") }
    
    val backupDir = File("backup")
    
    fun log(msg: String) {
        logs = logs + msg
    }
    
    suspend fun getDeviceName(): String {
        return try {
            AdbClient.execute(listOf("shell", "getprop", "ro.product.model"), timeoutSeconds = 5).trim().ifEmpty { "Android Device" }
        } catch (e: Exception) { "Android Device" }
    }

    LaunchedEffect(Unit) {
        val dev = AdbClient.checkDevices()
        devices = dev
        isConnected = dev.isNotEmpty()
        if (isConnected) log("Device connected: ${dev.first()}")
    }

    LaunchedEffect(isConnected) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            val dev = AdbClient.checkDevices()
            devices = dev
            val wasConnected = isConnected
            isConnected = dev.isNotEmpty()
            if (!wasConnected && isConnected) log("Device connected: ${dev.first()}")
            else if (wasConnected && !isConnected) {
                log("Device disconnected")
                scanJob?.cancel()
                isScanning = false
                isTransferring = false
                categoryData = emptyList()
                scanComplete = false
            }
        }
    }

    // Dialogs
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    
    if (showSuccessDialog) {
        BackupSuccessDialog(
            fileCount = backupStats.fileCount,
            totalSize = backupStats.totalSize,
            backupPath = backupStats.path,
            onOpenFolder = {
                SystemOpener.openFolder(backupDir.absolutePath)
                showSuccessDialog = false
            },
            onDonate = {
                SystemOpener.openInBrowser(AppConfig.DONATE_URL)
            },
            onClose = { showSuccessDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        // Show BackupManagerScreen or Main content
        if (showBackupManager) {
            BackupManagerScreen(
                onBack = { showBackupManager = false },
                onLog = { log(it) }
            )
        } else {
            Scaffold(

            topBar = {
                TopAppBar(
                    title = { Text(AppConfig.APP_NAME, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold) },
                    backgroundColor = AppColors.Surface,
                    elevation = 0.dp,
                    actions = {
                        // Refresh button
                        IconButton(onClick = {
                            scope.launch {
                                val dev = AdbClient.checkDevices()
                                devices = dev
                                isConnected = dev.isNotEmpty()
                                log("Device refresh: ${if (isConnected) dev.first() else "Not connected"}")
                            }
                        }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = AppColors.TextPrimary)
                        }
                        
                        // Donate button (Gold Heart)
                        IconButton(onClick = { SystemOpener.openInBrowser(AppConfig.DONATE_URL) }) {
                            Icon(Icons.Default.Favorite, "Donate", tint = AppColors.TextPrimary)
                        }
                        
                        // More options menu
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, "Menu", tint = AppColors.TextPrimary)
                            }
                            TopBarMenu(
                                expanded = showMenu,
                                onDismiss = { showMenu = false },
                                onBackupManager = { showBackupManager = true },
                                onAbout = { showAboutDialog = true },
                                onCheckUpdates = { log("Check for updates...") },
                                onDonate = { SystemOpener.openInBrowser(AppConfig.DONATE_URL) }
                            )
                        }

                    }
                )
            },
            backgroundColor = AppColors.Background
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Tabs
                TabRow(
                    selectedTabIndex = currentTab,
                    backgroundColor = AppColors.Surface,
                    contentColor = AppColors.Primary
                ) {
                    Tab(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        text = { Text("BACKUP", color = if (currentTab == 0) AppColors.Primary else AppColors.TextSecondary) }
                    )
                    Tab(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        text = { Text("RESTORE", color = if (currentTab == 1) AppColors.Primary else AppColors.TextSecondary) }
                    )
                }

                // Content
                Box(modifier = Modifier.weight(1f).padding(24.dp)) {
                    if (currentTab == 0) {
                        BackupTab(
                            isConnected = isConnected,
                            isScanning = isScanning,
                            scanProgress = scanProgress,
                            scanStatus = scanStatus,
                            scanComplete = scanComplete,
                            isTransferring = isTransferring,
                            categoryData = categoryData,
                            selectedCategories = selectedCategories,
                            allFilesSelected = allFilesSelected,
                            progress = progress,
                            progressText = progressText,
                            onScanClick = {
                                isScanning = true
                                scanComplete = false
                                scanProgress = 0f
                                log("Starting Full Scan...")
                                
                                scanJob = scope.launch {
                                    try {
                                        scanStatus = "Scanning files..."
                                        scanProgress = 0.1f
                                        val fileResults = FileScanner.scanAllFiles()
                                        scanProgress = 0.5f
                                        
                                        scanStatus = "Scanning installed apps..."
                                        scanProgress = 0.6f
                                        installedApps = AppEngine.scanInstalledApps()
                                        scanProgress = 0.8f
                                        
                                        scanStatus = "Checking system data..."
                                        scanProgress = 0.9f
                                        
                                        categoryData = CategoryManager.createCategoryData(
                                            scanResults = fileResults,
                                            installedAppsCount = installedApps.size,
                                            contactsCount = 1,
                                            callLogsCount = 1
                                        )
                                        
                                        categoryData.forEach { data ->
                                            if (data.count > 0) log("  ${data.displayName}: ${data.count}")
                                        }
                                        log("Total: ${categoryData.sumOf { it.count }} items found")
                                        
                                        scanProgress = 1.0f
                                        scanStatus = "Scan complete!"
                                        
                                        selectedCategories = categoryData
                                            .filter { it.count > 0 && !it.experimental }
                                            .map { it.category }
                                            .toSet()
                                        
                                        scanComplete = true
                                    } catch (e: Exception) {
                                        log("Scan failed: ${e.message}")
                                        scanStatus = "Scan failed"
                                    } finally {
                                        isScanning = false
                                    }
                                }
                            },
                            onCancelScan = {
                                scanJob?.cancel()
                                isScanning = false
                                scanProgress = 0f
                                scanStatus = ""
                                log("Scan cancelled")
                            },
                            onBackupClick = {
                                isTransferring = true
                                var totalFiles = 0
                                var totalBytes = 0L
                                
                                scope.launch {
                                    try {
                                        backupDir.mkdirs()
                                        val deviceName = getDeviceName()
                                        val backedUpFiles = mutableListOf<TransferableItem>()
                                        val backedUpApps = mutableListOf<InstalledApp>()
                                        var contactsCount = 0
                                        var callLogsCount = 0
                                        
                                        if (allFilesSelected) {
                                            log("Starting ALL FILES backup...")
                                            progress = 0.1f
                                            progressText = "Pulling entire storage..."
                                            
                                            val sdcardLocal = File(backupDir, "sdcard")
                                            sdcardLocal.mkdirs()
                                            
                                            AdbClient.execute(listOf("pull", "/sdcard/", sdcardLocal.absolutePath), timeoutSeconds = 3600)
                                            File(sdcardLocal, "Android/data").deleteRecursively()
                                            
                                            // Count files
                                            sdcardLocal.walk().filter { it.isFile }.forEach { 
                                                totalFiles++
                                                totalBytes += it.length()
                                            }
                                            progress = 0.8f
                                        } else {
                                            val fileCategories = listOf(Category.IMAGES, Category.VIDEOS, Category.AUDIO, 
                                                                        Category.ARCHIVES, Category.DOCS, Category.OTHERS)
                                            val filesToBackup = categoryData
                                                .filter { it.category in selectedCategories && it.category in fileCategories }
                                                .flatMap { it.items }
                                            
                                            if (filesToBackup.isNotEmpty()) {
                                                log("Backing up ${filesToBackup.size} files...")
                                                val engine = BackupEngine()
                                                engine.backup(filesToBackup, backupDir).collect { p ->
                                                    progress = p.processedCount.toFloat() / p.totalCount.toFloat() * 0.5f
                                                    progressText = "Files: ${p.processedCount}/${p.totalCount}"
                                                }
                                                backedUpFiles.addAll(filesToBackup)
                                                totalFiles += filesToBackup.size
                                            }
                                        }
                                        
                                        if (Category.INSTALLED_APPS in selectedCategories && installedApps.isNotEmpty()) {
                                            log("Backing up ${installedApps.size} apps...")
                                            AppEngine.backupApps(installedApps, backupDir) { current, total, _ ->
                                                progress = 0.5f + (current.toFloat() / total.toFloat() * 0.3f)
                                                progressText = "Apps: $current/$total"
                                            }
                                            backedUpApps.addAll(installedApps)
                                            totalFiles += installedApps.size
                                        }
                                        
                                        if (Category.CONTACTS in selectedCategories) {
                                            log("Backing up contacts...")
                                            progress = 0.85f
                                            progressText = "Contacts..."
                                            contactsCount = DataEngine.backupContacts(backupDir)
                                            totalFiles += contactsCount
                                        }
                                        
                                        if (Category.CALL_LOGS in selectedCategories) {
                                            log("Backing up call logs...")
                                            progress = 0.95f
                                            progressText = "Call logs..."
                                            callLogsCount = DataEngine.backupCallLogs(backupDir)
                                        }
                                        
                                        ManifestManager.createManifest(backupDir, deviceName, backedUpFiles, backedUpApps, contactsCount, callLogsCount)
                                        
                                        // Calculate total size
                                        if (totalBytes == 0L) {
                                            backupDir.walk().filter { it.isFile }.forEach { totalBytes += it.length() }
                                        }
                                        
                                        progress = 1f
                                        log("✅ Backup complete!")
                                        
                                        // Show success dialog
                                        backupStats = BackupStats(
                                            fileCount = totalFiles,
                                            totalSize = formatSize(totalBytes),
                                            path = backupDir.absolutePath
                                        )
                                        showSuccessDialog = true
                                        
                                    } catch (e: Exception) {
                                        log("Backup error: ${e.message}")
                                    } finally {
                                        isTransferring = false
                                        progress = 0f
                                    }
                                }
                            },
                            onCategoryToggle = { category, checked ->
                                selectedCategories = if (checked) selectedCategories + category else selectedCategories - category
                            },
                            onAllFilesToggle = { checked ->
                                allFilesSelected = checked
                            }
                        )
                    } else {
                        RestoreTab(
                            isConnected = isConnected,
                            isRestoring = isTransferring,
                            progress = progress,
                            progressText = progressText,
                            backupDir = backupDir,
                            onRestoreClick = { selectedRestoreCategories ->
                                isTransferring = true
                                
                                scope.launch {
                                    try {
                                        val manifest = ManifestManager.readManifest(backupDir)
                                            ?: ManifestManager.scanBackupFolder(backupDir)
                                        
                                        var totalItems = 0
                                        var processed = 0
                                        
                                        manifest.categories.forEach { (name, items) ->
                                            if (name in selectedRestoreCategories) totalItems += items.size
                                        }
                                        
                                        manifest.categories.forEach { (categoryName, items) ->
                                            if (categoryName !in selectedRestoreCategories) return@forEach
                                            
                                            log("Restoring $categoryName (${items.size} items)...")
                                            
                                            items.forEach { item ->
                                                progress = if (totalItems > 0) processed.toFloat() / totalItems else 0f
                                                progressText = item.localPath.substringAfterLast("/")
                                                
                                                val localFile = File(backupDir, item.localPath)
                                                
                                                when (item.type) {
                                                    "app" -> {
                                                        if (localFile.exists()) {
                                                            try { 
                                                                log("Installing ${localFile.name}...")
                                                                AdbClient.execute(listOf("install", "-r", localFile.absolutePath), timeoutSeconds = 120) 
                                                            } catch (e: Exception) { log("Failed to install: ${localFile.name}") }
                                                        }
                                                    }
                                                    "contact" -> {
                                                        if (localFile.exists()) {
                                                            try {
                                                                log("Restoring Contacts...")
                                                                val tempPath = "/sdcard/restore_contacts.vcf"
                                                                AdbClient.execute(listOf("push", localFile.absolutePath, tempPath), timeoutSeconds = 60)
                                                                // Trigger import intent
                                                                AdbClient.execute(listOf("shell", "am", "start", "-t", "text/x-vcard", "-d", "file://$tempPath", "-a", "android.intent.action.VIEW", "com.android.contacts"), timeoutSeconds = 10)
                                                                log("Contacts import launched on device")
                                                            } catch (e: Exception) { log("Failed to restore contacts: ${e.message}") }
                                                        }
                                                    }
                                                    "file" -> {
                                                        if (localFile.exists() && item.remotePath.isNotEmpty()) {
                                                            try { 
                                                                // Ensure parent dir exists (optional but safe)
                                                                // val parentDir = item.remotePath.substringBeforeLast("/")
                                                                // AdbClient.execute(listOf("shell", "mkdir", "-p", parentDir))
                                                                AdbClient.execute(listOf("push", localFile.absolutePath, item.remotePath), timeoutSeconds = 60)
                                                            } catch (e: Exception) { log("Failed to push: ${localFile.name}") }
                                                        }
                                                    }
                                                }
                                                processed++
                                            }
                                        }
                                        
                                        progress = 1f
                                        log("✅ Restore complete!")
                                        
                                    } catch (e: Exception) {
                                        log("Restore error: ${e.message}")
                                    } finally {
                                        isTransferring = false
                                        progress = 0f
                                    }
                                }
                            }
                        )
                    }
                }

                // View Logs button
                if (!showLogs) {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                        TextButton(
                            onClick = { showLogs = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.TextSecondary)
                        ) {
                            Icon(Icons.Default.Terminal, "Logs", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("View Logs", fontSize = 14.sp)
                        }
                    }
                }
                
                // Log panel
                AnimatedVisibility(
                    visible = showLogs,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Column {
                        Divider(color = AppColors.Divider)
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(AppColors.LogBackground)) {
                            IconButton(onClick = { showLogs = false }, modifier = Modifier.align(Alignment.TopEnd)) {
                                Icon(Icons.Default.Close, "Close", tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                            }
                            
                            val listState = rememberLazyListState()
                            LaunchedEffect(logs.size) { if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex) }
                            
                            LazyColumn(state = listState, modifier = Modifier.padding(start = 12.dp, end = 40.dp, top = 8.dp, bottom = 8.dp)) {
                                items(logs) { logMsg ->
                                    Text(logMsg, color = AppColors.LogText, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
            }
        } // end Scaffold
        } // end else
    }
}

data class BackupStats(val fileCount: Int, val totalSize: String, val path: String)

fun formatSize(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.2f MB", bytes / 1_048_576.0)
        bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = AppConfig.APP_NAME,
        icon = painterResource("icon.png")
    ) {
        App()
    }
}