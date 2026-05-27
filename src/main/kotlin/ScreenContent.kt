import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ui.components.DashboardScreen
import java.io.File

@Composable
fun MainScaffold(
    currentScreen: Screen,
    deviceName: String,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onBackupManagerClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogMessage: (String) -> Unit,
    screenContent: @Composable () -> Unit,
    logs: List<String>,
    showLogs: Boolean,
    onShowLogsChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (currentScreen != Screen.HOME) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, "Back")
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(20.dp), tint = AppColors.Primary)
                        Spacer(Modifier.width(12.dp))
                        Text(deviceName, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                    }
                },
                backgroundColor = AppColors.Surface,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = { SystemOpener.openInBrowser(AppConfig.DONATE_URL) }) {
                        Icon(Icons.Default.Favorite, "Donate", tint = AppColors.TextPrimary)
                    }
                    Box {
                        IconButton(onClick = { onShowMenuChange(true) }) {
                            Icon(Icons.Default.MoreVert, "Menu", tint = AppColors.TextPrimary)
                        }
                        TopBarMenu(
                            expanded = showMenu,
                            onDismiss = { onShowMenuChange(false) },
                            onBackupManager = onBackupManagerClick,
                            onAbout = onAboutClick,
                            onCheckUpdates = { onLogMessage("Coming soon...") },
                            onDonate = { SystemOpener.openInBrowser(AppConfig.DONATE_URL) }
                        )
                    }
                }
            )
        },
        backgroundColor = AppColors.Background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f)) { screenContent() }

            if (!showLogs) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    TextButton(
                        onClick = { onShowLogsChange(true) },
                        colors = ButtonDefaults.textButtonColors(contentColor = AppColors.TextSecondary)
                    ) {
                        Icon(Icons.Default.Terminal, "Logs", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("View Logs", fontSize = 14.sp)
                    }
                }
            }

            AnimatedVisibility(
                visible = showLogs,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Column {
                    Divider(color = AppColors.Divider)
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(AppColors.LogBackground)) {
                        IconButton(onClick = { onShowLogsChange(false) }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Default.Close, "Close", tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        val listState = rememberLazyListState()
                        LaunchedEffect(logs.size) {
                            if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
                        }
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.padding(start = 12.dp, end = 40.dp, top = 8.dp, bottom = 8.dp)
                        ) {
                            items(logs) { logMsg ->
                                Text(logMsg, color = AppColors.LogText, fontSize = 11.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenContent(
    currentScreen: Screen,
    isConnected: Boolean,
    isScanning: Boolean,
    scanProgress: Float,
    scanStatus: String,
    scanComplete: Boolean,
    transferMode: TransferMode,
    categoryData: List<CategoryData>,
    selectedCategories: Set<Category>,
    allFilesSelected: Boolean,
    progress: Float,
    progressText: String,
    installedApps: List<InstalledApp>,
    selectedPackageNames: Set<String>,
    backupDir: File,
    scope: CoroutineScope,
    scanJob: Job?,
    onScanJobChange: (Job?) -> Unit,
    onScanningChange: (Boolean) -> Unit,
    onScanProgressChange: (Float) -> Unit,
    onScanStatusChange: (String) -> Unit,
    onScanCompleteChange: (Boolean) -> Unit,
    onCategoryDataChange: (List<CategoryData>) -> Unit,
    onSelectedCategoriesChange: (Set<Category>) -> Unit,
    onInstalledAppsChange: (List<InstalledApp>) -> Unit,
    onAllFilesSelectedChange: (Boolean) -> Unit,
    onTransferModeChange: (TransferMode) -> Unit,
    onProgressChange: (Float) -> Unit,
    onProgressTextChange: (String) -> Unit,
    onSelectedPackageNamesChange: (Set<String>) -> Unit,
    onBackupStatsChange: (BackupStats) -> Unit,
    onShowSuccessDialog: () -> Unit,
    onLog: (String) -> Unit,
    onNavigateTo: (Screen) -> Unit,
) {
    when (currentScreen) {
        Screen.HOME -> {
    DashboardScreen(
        onBackupClick = { onNavigateTo(Screen.BACKUP) },
        onRestoreClick = { onNavigateTo(Screen.RESTORE) }
    )
        }
        Screen.BACKUP -> {
            LaunchedEffect(Unit) {
                if (!scanComplete && !isScanning) {
                    onScanningChange(true)
                    onScanProgressChange(0f)
                    onLog("Auto-starting Scan...")
                    val job = scope.launch {
                        try {
                            onScanStatusChange("Scanning files...")
                            onScanProgressChange(0.1f)
                            val fileResults = FileScanner.scanAllFiles()
                            onScanProgressChange(0.5f)
                            onScanStatusChange("Scanning installed apps...")
                            onScanProgressChange(0.6f)
                            val apps = AppEngine.scanInstalledApps()
                            onInstalledAppsChange(apps)
                            onScanProgressChange(0.8f)
                            onScanStatusChange("Checking system data...")
                            onScanProgressChange(0.9f)
                            val catData = CategoryManager.createCategoryData(
                                scanResults = fileResults,
                                installedAppsCount = apps.size,
                                contactsCount = 1,
                                callLogsCount = 1
                            )
                            onCategoryDataChange(catData)
                            catData.forEach { if (it.count > 0) onLog("  ${it.displayName}: ${it.count}") }
                            onLog("Total: ${catData.sumOf { it.count }} items found")
                            onScanProgressChange(1.0f)
                            onScanStatusChange("Scan complete!")
                            onSelectedCategoriesChange(catData.filter { it.count > 0 && !it.experimental }.map { it.category }.toSet())
                            onScanCompleteChange(true)
                        } catch (e: Exception) {
                            onLog("Scan failed: ${e.message}")
                            onScanStatusChange("Scan failed")
                        } finally {
                            onScanningChange(false)
                        }
                    }
                    onScanJobChange(job)
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                BackupTab(
                    isConnected = isConnected,
                    isScanning = isScanning,
                    scanProgress = scanProgress,
                    scanStatus = scanStatus,
                    scanComplete = scanComplete,
                    isTransferring = transferMode == TransferMode.BACKUP,
                    categoryData = categoryData,
                    selectedCategories = selectedCategories,
                    allFilesSelected = allFilesSelected,
                    progress = progress,
                    progressText = progressText,
                    installedApps = installedApps,
                    selectedPackageNames = selectedPackageNames,
                    onUpdateSelectedApps = { newSet ->
                        onSelectedPackageNamesChange(newSet)
                        if (newSet.isNotEmpty())
                            onSelectedCategoriesChange(selectedCategories + Category.INSTALLED_APPS)
                    },
                    onScanClick = {
                        onScanningChange(true)
                        onScanCompleteChange(false)
                        onScanProgressChange(0f)
                        onLog("Starting Full Scan...")
                        val job = scope.launch {
                            try {
                                onScanStatusChange("Scanning files...")
                                onScanProgressChange(0.1f)
                                val fileResults = FileScanner.scanAllFiles()
                                onScanProgressChange(0.5f)
                                onScanStatusChange("Scanning installed apps...")
                                onScanProgressChange(0.6f)
                                val apps = AppEngine.scanInstalledApps()
                                onInstalledAppsChange(apps)
                                onScanProgressChange(0.8f)
                                onScanStatusChange("Checking system data...")
                                onScanProgressChange(0.9f)
                                val catData = CategoryManager.createCategoryData(
                                    scanResults = fileResults,
                                    installedAppsCount = apps.size,
                                    contactsCount = 1,
                                    callLogsCount = 1
                                )
                                onCategoryDataChange(catData)
                                catData.forEach { if (it.count > 0) onLog("  ${it.displayName}: ${it.count}") }
                                onLog("Total: ${catData.sumOf { it.count }} items found")
                                onScanProgressChange(1.0f)
                                onScanStatusChange("Scan complete!")
                                onSelectedCategoriesChange(catData.filter { it.count > 0 && !it.experimental }.map { it.category }.toSet())
                                onScanCompleteChange(true)
                            } catch (e: Exception) {
                                onLog("Scan failed: ${e.message}")
                                onScanStatusChange("Scan failed")
                            } finally {
                                onScanningChange(false)
                            }
                        }
                        onScanJobChange(job)
                    },
                    onCancelScan = {
                        scanJob?.cancel()
                        onScanningChange(false)
                        onScanProgressChange(0f)
                        onScanStatusChange("")
                        onLog("Scan cancelled")
                    },
                    onBackupClick = {
                        onTransferModeChange(TransferMode.BACKUP)
                        var totalFiles = 0
                        var totalBytes = 0L
                        scope.launch {
                            try {
                                backupDir.mkdirs()
                                val backedUpFiles = mutableListOf<TransferableItem>()
                                val backedUpApps = mutableListOf<InstalledApp>()
                                var contactsCount = 0
                                var callLogsCount = 0

                                if (allFilesSelected) {
                                    onLog("Starting ALL FILES backup...")
                                    onProgressChange(0.1f)
                                    onProgressTextChange("Pulling entire storage...")
                                    val sdcardLocal = File(backupDir, "sdcard").also { it.mkdirs() }
                                    AdbClient.execute(listOf("pull", "/sdcard/", sdcardLocal.absolutePath), timeoutSeconds = 3600)
                                    File(sdcardLocal, "Android/data").deleteRecursively()
                                    sdcardLocal.walk().filter { it.isFile }.forEach { totalFiles++; totalBytes += it.length() }
                                    onProgressChange(0.8f)
                                } else {
                                    val fileCategories = listOf(Category.IMAGES, Category.VIDEOS, Category.AUDIO, Category.ARCHIVES, Category.DOCS, Category.OTHERS)
                                    val filesToBackup = categoryData.filter { it.category in selectedCategories && it.category in fileCategories }.flatMap { it.items }
                                    if (filesToBackup.isNotEmpty()) {
                                        onLog("Backing up ${filesToBackup.size} files...")
                                        BackupEngine().backup(filesToBackup, backupDir).collect { p ->
                                            onProgressChange(p.processedCount.toFloat() / p.totalCount.toFloat() * 0.5f)
                                            onProgressTextChange("Files: ${p.processedCount}/${p.totalCount}")
                                        }
                                        backedUpFiles.addAll(filesToBackup)
                                        totalFiles += filesToBackup.size
                                    }
                                }

                                if (Category.INSTALLED_APPS in selectedCategories && installedApps.isNotEmpty()) {
                                    onLog("Backing up ${installedApps.size} apps...")
                                    AppEngine.backupApps(installedApps, backupDir) { current, total, _ ->
                                        onProgressChange(0.5f + current.toFloat() / total.toFloat() * 0.3f)
                                        onProgressTextChange("Apps: $current/$total")
                                    }
                                    backedUpApps.addAll(installedApps)
                                    totalFiles += installedApps.size
                                }

                                if (Category.CONTACTS in selectedCategories) {
                                    onLog("Backing up contacts...")
                                    onProgressChange(0.85f)
                                    onProgressTextChange("Contacts...")
                                    contactsCount = DataEngine.backupContacts(backupDir)
                                    totalFiles += contactsCount
                                }

                                if (Category.CALL_LOGS in selectedCategories) {
                                    onLog("Backing up call logs...")
                                    onProgressChange(0.95f)
                                    onProgressTextChange("Call logs...")
                                    callLogsCount = DataEngine.backupCallLogs(backupDir)
                                }

                                ManifestManager.createManifest(backupDir, /* deviceName */ "", backedUpFiles, backedUpApps, contactsCount, callLogsCount)

                                if (totalBytes == 0L) backupDir.walk().filter { it.isFile }.forEach { totalBytes += it.length() }

                                onProgressChange(1f)
                                onLog("✅ Backup complete!")
                                onBackupStatsChange(BackupStats(totalFiles, formatSize(totalBytes), backupDir.absolutePath))
                                onShowSuccessDialog()
                            } catch (e: Exception) {
                                onLog("Backup error: ${e.message}")
                            } finally {
                                onTransferModeChange(TransferMode.NONE)
                                onProgressChange(0f)
                            }
                        }
                    },
                    onCategoryToggle = { category, checked ->
                        onSelectedCategoriesChange(if (checked) selectedCategories + category else selectedCategories - category)
                    },
                    onAllFilesToggle = { onAllFilesSelectedChange(it) }
                )
            }
        }

        Screen.RESTORE -> {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                RestoreTab(
                    isConnected = isConnected,
                    isRestoring = transferMode == TransferMode.RESTORE,
                    progress = progress,
                    progressText = progressText,
                    backupDir = backupDir,
                    onRestoreClick = { selectedRestoreCategories ->
                        onTransferModeChange(TransferMode.RESTORE)
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
                                    onLog("Restoring $categoryName (${items.size} items)...")
                                    items.forEach { item ->
                                        onProgressChange(if (totalItems > 0) processed.toFloat() / totalItems else 0f)
                                        onProgressTextChange(item.localPath.substringAfterLast("/"))
                                        val localFile = File(backupDir, item.localPath)
                                        when (item.type) {
                                            "app" -> if (localFile.exists()) try {
                                                onLog("Installing ${localFile.name}...")
                                                AdbClient.execute(listOf("install", "-r", localFile.absolutePath), timeoutSeconds = 120)
                                            } catch (e: Exception) { onLog("Failed to install: ${localFile.name}") }
                                            "contact" -> if (localFile.exists()) try {
                                                onLog("Restoring Contacts...")
                                                val tempPath = "/sdcard/restore_contacts.vcf"
                                                AdbClient.execute(listOf("push", localFile.absolutePath, tempPath), timeoutSeconds = 60)
                                                AdbClient.execute(listOf("shell", "am", "start", "-t", "text/x-vcard", "-d", "file://$tempPath", "-a", "android.intent.action.VIEW", "com.android.contacts"), timeoutSeconds = 10)
                                                onLog("Contacts import launched on device")
                                            } catch (e: Exception) { onLog("Failed to restore contacts: ${e.message}") }
                                            "file" -> if (localFile.exists() && item.remotePath.isNotEmpty()) try {
                                                AdbClient.execute(listOf("push", localFile.absolutePath, item.remotePath), timeoutSeconds = 60)
                                            } catch (e: Exception) { onLog("Failed to push: ${localFile.name}") }
                                        }
                                        processed++
                                    }
                                }
                                onProgressChange(1f)
                                onLog("✅ Restore complete!")
                            } catch (e: Exception) {
                                onLog("Restore error: ${e.message}")
                            } finally {
                                onTransferModeChange(TransferMode.NONE)
                                onProgressChange(0f)
                            }
                        }
                    }
                )
            }
        }
    }
}

fun formatSize(bytes: Long): String = when {
    bytes >= 1_073_741_824 -> String.format("%.2f GB", bytes / 1_073_741_824.0)
    bytes >= 1_048_576    -> String.format("%.2f MB", bytes / 1_048_576.0)
    bytes >= 1024          -> String.format("%.2f KB", bytes / 1024.0)
    else                   -> "$bytes B"
}

