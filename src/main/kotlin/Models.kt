
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
