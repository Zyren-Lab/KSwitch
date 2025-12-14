
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

data class TransferProgress(
    val currentFile: String,
    val processedCount: Int,
    val totalCount: Int
)

/**
 * BackupEngine - Handles file transfers with path mirroring
 */
class BackupEngine {

    /**
     * Backup files from device to local storage
     * Mirrors the folder structure exactly
     */
    fun backup(items: List<TransferableItem>, backupRoot: File): Flow<TransferProgress> = flow {
        var processed = 0
        val total = items.size

        items.forEach { item ->
            val localFile = File(backupRoot, item.localRelativePath)
            
            // Create parent directories
            localFile.parentFile?.mkdirs()

            emit(TransferProgress(item.name, processed, total))
            
            try {
                // adb pull with QUOTED paths to handle spaces
                AdbClient.execute(listOf("pull", item.remotePath, localFile.absolutePath))
            } catch (e: Exception) {
                println("Failed to pull ${item.remotePath}: ${e.message}")
            }
            
            processed++
            emit(TransferProgress(item.name, processed, total))
        }
    }

    /**
     * Restore files from local storage back to device
     */
    fun restore(items: List<TransferableItem>, backupRoot: File): Flow<TransferProgress> = flow {
        var processed = 0
        val total = items.size

        items.forEach { item ->
            val localFile = File(backupRoot, item.localRelativePath)
            
            emit(TransferProgress(item.name, processed, total))
            
            if (localFile.exists()) {
                try {
                    when (item.type) {
                        ItemType.APP -> {
                            // Install APK
                            AdbClient.execute(listOf("install", "-r", localFile.absolutePath))
                        }
                        else -> {
                            // Push file back to original location
                            AdbClient.execute(listOf("push", localFile.absolutePath, item.remotePath))
                        }
                    }
                } catch (e: Exception) {
                    println("Failed to restore ${item.name}: ${e.message}")
                }
            }
            
            processed++
            emit(TransferProgress(item.name, processed, total))
        }
    }
}
