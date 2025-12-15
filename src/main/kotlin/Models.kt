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
 
// Data models for KSwitch - Final Edition

enum class Category {
    IMAGES,
    VIDEOS,
    AUDIO,
    ARCHIVES,       // .zip, .rar, .7z, .apk files in storage
    DOCS,           // .pdf, .doc, .txt, .xlsx
    OTHERS,         // Everything else from storage
    INSTALLED_APPS, // Real installed applications (pm list)
    CONTACTS,       // Experimental - from content provider
    CALL_LOGS       // Backup only - from call_log
}

enum class ItemType {
    IMAGE,
    VIDEO,
    APP,
    FILE
}

data class ScannedFile(
    val path: String,
    val category: Category,
    val fileName: String,
    val mimeType: String
)

data class TransferableItem(
    val name: String,
    val remotePath: String,
    val localRelativePath: String,
    val type: ItemType
)

data class InstalledApp(
    val packageName: String,
    val apkPath: String
)

data class ContactEntry(
    val name: String,
    val phone: String
)

data class CallLogEntry(
    val number: String,
    val date: String,
    val duration: String,
    val type: String // 1=incoming, 2=outgoing, 3=missed
)
