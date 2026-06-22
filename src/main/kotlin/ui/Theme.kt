/*
 * KSwitch - The GUI Backup Tool for Linux
 * Copyright (C) 2025-2026 Zyren-Lab
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
 
import androidx.compose.ui.graphics.Color

// KSwitch Dark Theme Colors
object AppColors {
    var isDark = false

    // Primary
    val Primary = Color(0xFF0F6BFF)       //  Blue
    val PrimaryDark = Color(0xFF0A4DB8)
    val Accent = Color(0xFF00D9FF)        
    
    // Background
    val Background get() = if (isDark) Color(0xFF121212) else Color(0xFFF5F7FA)
    val Surface get() = if (isDark) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
    val SurfaceLight get() = if (isDark) Color(0xFF2D2D2D) else Color(0xFFE8EEF5)
    
    // Text
    val TextPrimary get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF1E1E1E)
    val TextSecondary get() = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)
    val TextDisabled get() = if (isDark) Color(0xFF666666) else Color(0xFFA0A0A0)
    val TextDisabledReadable get() = if (isDark) Color(0xFF909090) else Color(0xFF808080) 
    
    // Status colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    
    // Special
    val Divider get() = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
    val LogBackground get() = if (isDark) Color(0xFF0A0A0A) else Color(0xFF1E1E1E)
    val LogText = Color(0xFF00FF88)
    val DonateGold = Color(0xFFFFD700)      
    val DonateColor = Color(0xFFFD00FF)
}

// Constants
object AppConfig {
    const val DONATE_URL = "https://buymeacoffee.com/ZyrenLab"
    const val VERSION = "1.1.0"
    const val APP_NAME = "KSwitch"
}

// Helper to open URLs/folders
object SystemOpener {
    fun openInBrowser(url: String): Boolean {
        return try {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("linux") -> arrayOf("xdg-open", url)
                os.contains("mac") -> arrayOf("open", url)
                os.contains("win") -> arrayOf("cmd", "/c", "start", url)
                else -> return false
            }
            Runtime.getRuntime().exec(command)
            true
        } catch (e: Exception) {
            println("Failed to open browser: ${e.message}")
            false
        }
    }
    
    fun openFolder(path: String): Boolean {
        return try {
            val os = System.getProperty("os.name").lowercase()
            val command = when {
                os.contains("linux") -> arrayOf("xdg-open", path)
                os.contains("mac") -> arrayOf("open", path)
                os.contains("win") -> arrayOf("explorer", path)
                else -> return false
            }
            Runtime.getRuntime().exec(command)
            true
        } catch (e: Exception) {
            println("Failed to open folder: ${e.message}")
            false
        }
    }
}

// Keep old name for compatibility
typealias UrlOpener = SystemOpener
