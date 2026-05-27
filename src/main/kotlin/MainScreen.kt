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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ui.components.DashboardScreen
import ui.components.EmptyStateScreen
import java.io.File

enum class Screen { HOME, BACKUP, RESTORE }
enum class TransferMode { NONE, BACKUP, RESTORE }
data class BackupStats(val fileCount: Int, val totalSize: String, val path: String)

@Composable
fun MainScreen() {
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf(emptyList<String>()) }
    var isConnected by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var logs by remember { mutableStateOf(listOf<String>()) }

    var showAppSelector by remember { mutableStateOf(false) }
    var selectedPackageNames by remember { mutableStateOf(setOf<String>()) }

    var showMenu by remember { mutableStateOf(false) }
    var showBackupManager by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var backupStats by remember { mutableStateOf(BackupStats(0, "0 B", "")) }

    var isScanning by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var scanStatus by remember { mutableStateOf("") }
    var scanJob by remember { mutableStateOf<Job?>(null) }
    var scanComplete by remember { mutableStateOf(false) }
    var categoryData by remember { mutableStateOf(emptyList<CategoryData>()) }
    var selectedCategories by remember { mutableStateOf(setOf<Category>()) }
    var allFilesSelected by remember { mutableStateOf(false) }

    var installedApps by remember { mutableStateOf(emptyList<InstalledApp>()) }
    var transferMode by remember { mutableStateOf(TransferMode.NONE) }
    var progress by remember { mutableStateOf(0f) }
    var progressText by remember { mutableStateOf("") }
    var deviceName by remember { mutableStateOf("Android Device") }

    var baseBackupPath by remember { mutableStateOf(File(System.getProperty("user.dir")).absolutePath) }
    val backupDir = File(baseBackupPath, "KSwitch-backup/${deviceName.replace(Regex("[^a-zA-Z0-9_\\-\\.]"), "_")}")

    fun log(msg: String) { logs = logs + msg }

    suspend fun fetchDeviceName(): String {
        return try {
            var name = AdbClient.execute(listOf("shell", "settings", "get", "global", "device_name"), timeoutSeconds = 5).trim()
            if (name.isEmpty() || name == "null" || name.contains("error")) {
                name = AdbClient.execute(listOf("shell", "getprop", "ro.product.model"), timeoutSeconds = 5).trim()
            }
            if (name.isEmpty() || name == "null") "Android Device" else name
        } catch (e: Exception) { "Android Device" }
    }

    LaunchedEffect(Unit) {
        val dev = AdbClient.checkDevices()
        devices = dev
        isConnected = dev.isNotEmpty()
        if (isConnected) {
            deviceName = fetchDeviceName()
            log("Device connected: $deviceName")
        }
    }

    LaunchedEffect(isConnected) {
        while (true) {
            kotlinx.coroutines.delay(2000)
            val dev = AdbClient.checkDevices()
            devices = dev
            val wasConnected = isConnected
            isConnected = dev.isNotEmpty()
            if (!wasConnected && isConnected) {
                deviceName = fetchDeviceName()
                log("Device connected: $deviceName")
            } else if (wasConnected && !isConnected) {
                log("Device disconnected")
                scanJob?.cancel()
                isScanning = false
                transferMode = TransferMode.NONE
                categoryData = emptyList()
                scanComplete = false
                currentScreen = Screen.HOME
            }
        }
    }

    if (showAboutDialog) AboutDialog(onDismiss = { showAboutDialog = false })
    if (showSuccessDialog) {
        BackupSuccessDialog(
            fileCount = backupStats.fileCount,
            totalSize = backupStats.totalSize,
            backupPath = backupStats.path,
            onOpenFolder = { SystemOpener.openFolder(backupDir.absolutePath); showSuccessDialog = false },
            onDonate = { SystemOpener.openInBrowser(AppConfig.DONATE_URL) },
            onClose = { showSuccessDialog = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
        if (!isConnected) {
            EmptyStateScreen(onScanClick = {
                scope.launch {
                    val dev = AdbClient.checkDevices()
                    devices = dev
                    isConnected = dev.isNotEmpty()
                    if (isConnected) {
                        deviceName = fetchDeviceName()
                        log("Device connected via scan: $deviceName")
                    } else log("No devices found")
                }
            })
        } else if (showBackupManager) {
            BackupManagerScreen(
                backupRoot = File(baseBackupPath), // pass the base path
                onBack = { showBackupManager = false }, 
                onLog = { log(it) },
                onChangePath = { newPath -> baseBackupPath = newPath }
            )
        } else {
            MainScaffold(
                currentScreen = currentScreen,
                deviceName = deviceName,
                showMenu = showMenu,
                onShowMenuChange = { showMenu = it },
                onBackClick = { currentScreen = Screen.HOME },
                onBackupManagerClick = { showBackupManager = true },
                onAboutClick = { showAboutDialog = true },
                onLogMessage = { log(it) },
                screenContent = {
                    ScreenContent(
                        currentScreen = currentScreen,
                        isConnected = isConnected,
                        isScanning = isScanning,
                        scanProgress = scanProgress,
                        scanStatus = scanStatus,
                        scanComplete = scanComplete,
                        transferMode = transferMode,
                        categoryData = categoryData,
                        selectedCategories = selectedCategories,
                        allFilesSelected = allFilesSelected,
                        progress = progress,
                        progressText = progressText,
                        installedApps = installedApps,
                        selectedPackageNames = selectedPackageNames,
                        backupDir = backupDir,
                        scope = scope,
                        scanJob = scanJob,
                        onScanJobChange = { scanJob = it },
                        onScanningChange = { isScanning = it },
                        onScanProgressChange = { scanProgress = it },
                        onScanStatusChange = { scanStatus = it },
                        onScanCompleteChange = { scanComplete = it },
                        onCategoryDataChange = { categoryData = it },
                        onSelectedCategoriesChange = { selectedCategories = it },
                        onInstalledAppsChange = { installedApps = it },
                        onAllFilesSelectedChange = { allFilesSelected = it },
                        onTransferModeChange = { transferMode = it },
                        onProgressChange = { progress = it },
                        onProgressTextChange = { progressText = it },
                        onSelectedPackageNamesChange = { selectedPackageNames = it },
                        onBackupStatsChange = { backupStats = it },
                        onShowSuccessDialog = { showSuccessDialog = true },
                        onLog = { log(it) },
                        onNavigateTo = { currentScreen = it },
                    )
                },
                logs = logs,
                showLogs = showLogs,
                onShowLogsChange = { showLogs = it }
            )
        }
    }
}
