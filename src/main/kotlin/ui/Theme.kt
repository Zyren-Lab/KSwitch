
import androidx.compose.ui.graphics.Color

// Samsung Smart Switch inspired colors
object AppColors {
    val SamsungBlue = Color(0xFF3652DB)
    val BackgroundWhite = Color(0xFFFFFFFF)
    val SurfaceGray = Color(0xFFF7F7F7)
    val TextDark = Color(0xFF333333)
    val TextGray = Color(0xFF777777)
    val WarningOrange = Color(0xFFFF9800)
    val SuccessGreen = Color(0xFF4CAF50)
    val ErrorRed = Color(0xFFF44336)
}

// Constants
object AppConfig {
    // Ko-fi donate link
    const val DONATE_URL = "https://www.paypal.com/donate?hosted_button_id=EXAMPLE123"
    const val VERSION = "1.0.0"
}

// Helper to open URLs
object UrlOpener {
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
}
