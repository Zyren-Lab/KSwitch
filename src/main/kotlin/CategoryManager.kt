
data class CategoryData(
    val category: Category,
    val items: List<TransferableItem>,
    val displayName: String,
    val count: Int = items.size,
    val isSystemData: Boolean = false, // For Installed Apps, Contacts, Call Logs
    val experimental: Boolean = false  // For experimental features
)

object CategoryManager {
    
    fun createCategoryData(
        scanResults: Map<Category, List<ScannedFile>>,
        installedAppsCount: Int = 0,
        contactsCount: Int = 0,
        callLogsCount: Int = 0
    ): List<CategoryData> {
        val fileCategories = listOf(Category.IMAGES, Category.VIDEOS, Category.AUDIO, Category.ARCHIVES, Category.DOCS, Category.OTHERS)
        
        val result = mutableListOf<CategoryData>()
        
        // File-based categories
        fileCategories.forEach { category ->
            val files = scanResults[category] ?: emptyList()
            result.add(CategoryData(
                category = category,
                items = FileScanner.toTransferableItems(files),
                displayName = when(category) {
                    Category.IMAGES -> "Images"
                    Category.VIDEOS -> "Videos"
                    Category.AUDIO -> "Audio"
                    Category.ARCHIVES -> "Archives (.zip, .apk)"
                    Category.DOCS -> "Documents"
                    Category.OTHERS -> "Other Files"
                    else -> category.name
                }
            ))
        }
        
        // System data categories
        result.add(CategoryData(
            category = Category.INSTALLED_APPS,
            items = emptyList(), // Handled separately by AppEngine
            displayName = "Installed Apps",
            count = installedAppsCount,
            isSystemData = true
        ))
        
        result.add(CategoryData(
            category = Category.CONTACTS,
            items = emptyList(),
            displayName = "Contacts (Experimental)",
            count = contactsCount,
            isSystemData = true,
            experimental = true
        ))
        
        result.add(CategoryData(
            category = Category.CALL_LOGS,
            items = emptyList(),
            displayName = "Call Logs (Backup Only)",
            count = callLogsCount,
            isSystemData = true,
            experimental = true
        ))
        
        return result
    }
    
    fun formatCount(count: Int): String {
        return when {
            count >= 1000 -> String.format("%,d", count)
            else -> count.toString()
        }
    }
}
