
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object AdbClient {

    suspend fun checkDevices(): List<String> = withContext(Dispatchers.IO) {
        try {
            val output = execute("devices")
            output.lines()
                .drop(1) // Skip "List of devices attached"
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("\t")
                    if (parts.size >= 2 && parts[1] == "device") {
                        parts[0]
                    } else null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun execute(command: String, timeoutSeconds: Long = 60): String = withContext(Dispatchers.IO) {
        val parts = command.split(" ").toTypedArray() // Simple split, callers should use array version for complex args
        runProcess(listOf("adb") + parts.toList(), timeoutSeconds)
    }
    
    // Safer version for complex arguments (quoted paths etc)
    suspend fun execute(args: List<String>, timeoutSeconds: Long = 60): String = withContext(Dispatchers.IO) {
        runProcess(listOf("adb") + args, timeoutSeconds)
    }

    private fun runProcess(command: List<String>, timeoutSeconds: Long): String {
        val processBuilder = ProcessBuilder(command)
        processBuilder.redirectErrorStream(true) 
        
        val process = processBuilder.start()
        
        val output = StringBuilder()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String?
        
        try {
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            
            if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
                process.destroy()
                throw Exception("ADB Timeout: ${command.joinToString(" ")}")
            }
            
            if (process.exitValue() != 0) {
                 // For some commands (like pm path), empty output or specific error codes might be expected, 
                 // but generally non-zero is failure. We allow caller to parse output for soft-errors.
                 // However, if ADB itself fails (e.g. device not found), we should probably throw.
                 if (output.toString().contains("error: device")) {
                     throw Exception("ADB Error: $output")
                 }
            }
        } catch (e: Exception) {
            throw Exception("ADB Execution Failed: ${e.message}")
        }
        
        return output.toString().trim()
    }
}
