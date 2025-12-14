
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
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
        colors = lightColors(
            primary = AppColors.SamsungBlue,
            background = AppColors.BackgroundWhite,
            surface = AppColors.SurfaceGray,
            onPrimary = Color.White,
            onBackground = AppColors.TextDark,
            onSurface = AppColors.TextDark
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
    
    // Drawer and screens state
    var showDrawer by remember { mutableStateOf(false) }
    var showBackupManager by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    
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
    
    // Get device name
    suspend fun getDeviceName(): String {
        return try {
            val model = AdbClient.execute(listOf("shell", "getprop", "ro.product.model"), timeoutSeconds = 5).trim()
            model.ifEmpty { "Android Device" }
        } catch (e: Exception) {
            "Android Device"
        }
    }

    // Device check on launch
    LaunchedEffect(Unit) {
        val dev = AdbClient.checkDevices()
        devices = dev
        isConnected = dev.isNotEmpty()
        if (isConnected) log("Device connected: ${dev.first()}")
    }

    // Device polling
    LaunchedEffect(isConnected) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            val dev = AdbClient.checkDevices()
            devices = dev
            val wasConnected = isConnected
            isConnected = dev.isNotEmpty()
            if (!wasConnected && isConnected) {
                log("Device connected: ${dev.first()}")
            } else if (wasConnected && !isConnected) {
                log("Device disconnected")
                scanJob?.cancel()
                isScanning = false
                isTransferring = false
                categoryData = emptyList()
                scanComplete = false
            }
        }
    }

    // About dialog
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content or Backup Manager
        if (showBackupManager) {
            BackupManagerScreen(
                onBack = { showBackupManager = false },
                onLog = { log(it) }
            )
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("KSwitch", color = AppColors.TextDark, fontWeight = FontWeight.SemiBold) },
                        backgroundColor = Color(0xFFE8F0FE),
                        elevation = 0.dp,
                        actions = {
                            IconButton(onClick = {
                                scope.launch {
                                    val dev = AdbClient.checkDevices()
                                    devices = dev
                                    isConnected = dev.isNotEmpty()
                                    log("Device refresh: ${if (isConnected) dev.first() else "Not connected"}")
                                }
                            }) {
                                Icon(Icons.Default.Refresh, "Refresh", tint = AppColors.TextDark)
                            }
                            IconButton(onClick = { showDrawer = true }) {
                                Icon(Icons.Default.Menu, "Menu", tint = AppColors.TextDark)
                            }
                        }
                    )
                },
                backgroundColor = AppColors.BackgroundWhite
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    // Tabs
                    TabRow(
                        selectedTabIndex = currentTab,
                        backgroundColor = Color.White,
                        contentColor = AppColors.SamsungBlue
                    ) {
                        Tab(selected = currentTab == 0, onClick = { currentTab = 0 }, text = { Text("BACKUP") })
                        Tab(selected = currentTab == 1, onClick = { currentTab = 1 }, text = { Text("RESTORE") })
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
                                            log("Scanning all media files...")
                                            val fileResults = FileScanner.scanAllFiles()
                                            scanProgress = 0.5f
                                            
                                            scanStatus = "Scanning installed apps..."
                                            scanProgress = 0.6f
                                            log("Scanning installed apps...")
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
                                                if (data.count > 0) {
                                                    log("  ${data.displayName}: ${data.count}")
                                                }
                                            }
                                            
                                            val totalItems = categoryData.sumOf { it.count }
                                            log("Total: $totalItems items found")
                                            
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
                                                
                                                AdbClient.execute(
                                                    listOf("pull", "/sdcard/", sdcardLocal.absolutePath),
                                                    timeoutSeconds = 3600
                                                )
                                                
                                                File(sdcardLocal, "Android/data").deleteRecursively()
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
                                                }
                                            }
                                            
                                            if (Category.INSTALLED_APPS in selectedCategories && installedApps.isNotEmpty()) {
                                                log("Backing up ${installedApps.size} apps...")
                                                AppEngine.backupApps(installedApps, backupDir) { current, total, _ ->
                                                    progress = 0.5f + (current.toFloat() / total.toFloat() * 0.3f)
                                                    progressText = "Apps: $current/$total"
                                                }
                                                backedUpApps.addAll(installedApps)
                                            }
                                            
                                            if (Category.CONTACTS in selectedCategories) {
                                                log("Backing up contacts...")
                                                progress = 0.85f
                                                progressText = "Contacts..."
                                                contactsCount = DataEngine.backupContacts(backupDir)
                                                log("Saved $contactsCount contacts")
                                            }
                                            
                                            if (Category.CALL_LOGS in selectedCategories) {
                                                log("Backing up call logs...")
                                                progress = 0.95f
                                                progressText = "Call logs..."
                                                callLogsCount = DataEngine.backupCallLogs(backupDir)
                                                log("Saved $callLogsCount call entries")
                                            }
                                            
                                            // Create manifest
                                            ManifestManager.createManifest(
                                                backupDir = backupDir,
                                                deviceName = deviceName,
                                                filesBackedUp = backedUpFiles,
                                                appsBackedUp = backedUpApps,
                                                contactsCount = contactsCount,
                                                callLogsCount = callLogsCount
                                            )
                                            
                                            progress = 1f
                                            log("✅ Backup complete! Manifest saved.")
                                            
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
                                    if (checked) {
                                        selectedCategories = selectedCategories.filter { 
                                            it in listOf(Category.INSTALLED_APPS, Category.CONTACTS, Category.CALL_LOGS)
                                        }.toSet()
                                    }
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
                                                if (name in selectedRestoreCategories) {
                                                    totalItems += items.size
                                                }
                                            }
                                            
                                            manifest.categories.forEach { (categoryName, items) ->
                                                if (categoryName !in selectedRestoreCategories) return@forEach
                                                
                                                log("Restoring $categoryName (${items.size} items)...")
                                                
                                                items.forEach { item ->
                                                    progress = if (totalItems > 0) processed.toFloat() / totalItems else 0f
                                                    progressText = "${item.localPath.substringAfterLast("/")}"
                                                    
                                                    val localFile = File(backupDir, item.localPath)
                                                    
                                                    when (item.type) {
                                                        "app" -> {
                                                            if (localFile.exists()) {
                                                                try {
                                                                    AdbClient.execute(listOf("install", "-r", localFile.absolutePath), timeoutSeconds = 120)
                                                                } catch (e: Exception) {
                                                                    log("Failed to install ${localFile.name}")
                                                                }
                                                            }
                                                        }
                                                        "file" -> {
                                                            if (localFile.exists() && item.remotePath.isNotEmpty()) {
                                                                try {
                                                                    AdbClient.execute(listOf("push", localFile.absolutePath, item.remotePath), timeoutSeconds = 60)
                                                                } catch (e: Exception) { }
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

                    // Log panel
                    Divider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Color(0xFF2B2B2B))
                            .padding(8.dp)
                    ) {
                        val listState = rememberLazyListState()
                        LaunchedEffect(logs.size) {
                            if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
                        }
                        LazyColumn(state = listState) {
                            items(logs) { logMsg ->
                                Text(
                                    logMsg, 
                                    color = Color.Green, 
                                    fontSize = 11.sp, 
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Right drawer overlay
        AnimatedVisibility(
            visible = showDrawer,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showDrawer = false }
                )
                
                DrawerContent(
                    onBackupManagerClick = { showBackupManager = true },
                    onDonateClick = {
                        val opened = UrlOpener.openInBrowser(AppConfig.DONATE_URL)
                        if (!opened) {
                            log("Could not open browser")
                        }
                    },
                    onAboutClick = { showAboutDialog = true },
                    onCloseDrawer = { showDrawer = false }
                )
            }
        }
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KSwitch",
        icon = painterResource("icon.png")
    ) {
        App()
    }
}