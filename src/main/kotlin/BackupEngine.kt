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
