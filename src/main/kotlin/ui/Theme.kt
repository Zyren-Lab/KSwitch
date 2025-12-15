
import androidx.compose.ui.graphics.Color

// KSwitch Dark Theme Colors
object AppColors {
    // Primary
    val Primary = Color(0xFF6C63FF)       // Purple accent
    val PrimaryDark = Color(0xFF5A52D5)
    val Accent = Color(0xFF00D9FF)        // Cyan accent
    
    // Background
    val Background = Color(0xFF121212)     // Main background
    val Surface = Color(0xFF1E1E1E)        // Card/surface background
    val SurfaceLight = Color(0xFF2D2D2D)   // Elevated surface
    
    // Text
    val TextPrimary = Color(0xFFFFFFFF)    // White text
    val TextSecondary = Color(0xFFB0B0B0)  // Gray text
    val TextDisabled = Color(0xFF666666)   // Disabled text
    val TextDisabledReadable = Color(0xFF909090) // Disabled but readable
    
    // Status colors
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Error = Color(0xFFF44336)
    
    // Special
    val Divider = Color(0xFF333333)
    val LogBackground = Color(0xFF0A0A0A)
    val LogText = Color(0xFF00FF88)
    val DonateGold = Color(0xFFFFD700)      
    val DonateColor = Color(0xFFFD00FF)
}

// Constants
object AppConfig {
    const val DONATE_URL = "https://buymeacoffee.com/ZyrenLab"
    const val VERSION = "1.0.0"
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
