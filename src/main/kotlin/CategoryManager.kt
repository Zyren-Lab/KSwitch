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
                    Category.AUDIO -> "Audios"
                    Category.ARCHIVES -> "Archives and Not Installed Apps"
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
