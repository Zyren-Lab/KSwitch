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
 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * FileScanner - "No File Left Behind" Edition
 * Uses the Diamond Command to scan ALL useful files from the device
 * NOTE: .apk files in storage go to ARCHIVES category (not INSTALLED_APPS)
 */
object FileScanner {
    
    // Extension sets for classification
    private val imageExtensions = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", 
        "raw", "dng", "cr2", "nef", "arw", "tiff", "tif", "svg", "ico"
    )

    // 2. Videos
    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "3gp", "webm", "flv", "wmv", 
        "m4v", "mpg", "mpeg", "ts", "vob"
    )

    // 3. Audios
    private val audioExtensions = setOf(
        "mp3", "wav", "m4a", "flac", "ogg", "aac", "wma", "opus", 
        "amr", "m4b", "mid", "midi", "ac3"
    )

    // 4. Docs
    private val docExtensions = setOf(
        "pdf", "doc", "docx", "txt", "xls", "xlsx", "ppt", "pptx", 
        "odt", "ods", "odp", "csv", "rtf", "epub", "mobi", 
        "xml", "json", "html", "htm", "log", "md", "msg", "bin"
    )

    // 5. Archives
    private val archiveExtensions = setOf(
        // Archives
        "zip", "rar", "7z", "tar", "gz", "bz2", "xz", "iso", "jar", "lz4", "md5", "sha1", "sha256", "sha512", "br", "dat",
        // Android Apps
        "apk", "apks", "xapk", "xapks", "obb" 
    )
    
    // Regex to parse ADB output - handles paths with spaces!
    private val rowPattern = Regex("""Row:\s*\d+\s+_data=(.*?),\s*media_type=(\d+),\s*mime_type=(.*)""")

    /**
     * Execute the Diamond Command and parse all files
     */
    suspend fun scanAllFiles(): Map<Category, List<ScannedFile>> = withContext(Dispatchers.IO) {
        val results = mutableMapOf<Category, MutableList<ScannedFile>>()
        // Only file-based categories
        listOf(Category.IMAGES, Category.VIDEOS, Category.AUDIO, Category.ARCHIVES, Category.DOCS, Category.OTHERS)
            .forEach { results[it] = mutableListOf() }
        
        try {
            // THE DIAMOND COMMAND
            val command = """content query --uri content://media/external/file --projection _data:media_type:mime_type --where "media_type=1 OR media_type=2 OR media_type=3 OR mime_type LIKE 'application/%' OR mime_type LIKE 'text/%' OR _data LIKE '%.apk' OR _data LIKE '%.zip' OR _data LIKE '%.7z' OR _data LIKE '%.rar'""""
            
            val output = AdbClient.execute(listOf("shell", command), timeoutSeconds = 300)
            
            output.lines().forEach { line ->
                parseAndCategorize(line, results)
            }
            
        } catch (e: Exception) {
            println("FileScanner error: ${e.message}")
            e.printStackTrace()
        }
        
        results
    }
    
    private fun parseAndCategorize(line: String, results: MutableMap<Category, MutableList<ScannedFile>>) {
        val match = rowPattern.find(line) ?: return
        
        val (path, mediaTypeStr, mimeType) = match.destructured
        val trimmedPath = path.trim()
        val mediaType = mediaTypeStr.toIntOrNull() ?: 0
        val trimmedMime = mimeType.trim()
        
        if (trimmedPath.isEmpty() || !trimmedPath.startsWith("/")) return
        
        // Get file extension
        val ext = trimmedPath.substringAfterLast('.', "").lowercase()
        val fileName = trimmedPath.substringAfterLast('/')
        
        // EXTENSION-FIRST classification
        // NOTE: APK files go to ARCHIVES (not INSTALLED_APPS)
        val category = when {
            ext in archiveExtensions -> Category.ARCHIVES
            ext in imageExtensions || mediaType == 1 -> Category.IMAGES
            ext in videoExtensions || mediaType == 3 -> Category.VIDEOS
            ext in audioExtensions || mediaType == 2 -> Category.AUDIO
            ext in docExtensions -> Category.DOCS
            trimmedMime.startsWith("application/") -> Category.DOCS
            trimmedMime.startsWith("text/") -> Category.DOCS
            else -> Category.OTHERS
        }
        
        results[category]?.add(ScannedFile(
            path = trimmedPath,
            category = category,
            fileName = fileName,
            mimeType = trimmedMime
        ))
    }
    
    /**
     * Convert scanned files to transferable items for backup
     */
    fun toTransferableItems(files: List<ScannedFile>): List<TransferableItem> {
        return files.map { file ->
            val localPath = when {
                file.path.startsWith("/storage/emulated/0/") -> {
                    "sdcard/" + file.path.removePrefix("/storage/emulated/0/")
                }
                file.path.startsWith("/sdcard/") -> {
                    file.path.removePrefix("/")
                }
                else -> {
                    "sdcard/other/" + file.fileName
                }
            }
            
            val itemType = when (file.category) {
                Category.IMAGES -> ItemType.IMAGE
                Category.VIDEOS -> ItemType.VIDEO
                else -> ItemType.FILE
            }
            
            TransferableItem(
                name = file.fileName,
                remotePath = file.path,
                localRelativePath = localPath,
                type = itemType
            )
        }
    }
}
