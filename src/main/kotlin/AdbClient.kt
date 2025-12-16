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
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.util.concurrent.TimeUnit

object AdbClient {

    // 1. ADB path
    private val adbPath: String by lazy {
        setupEmbeddedAdb() ?: "adb" // If embedded adb is not found, use system adb
    }

    // 2. Setup embedded ADB
    private fun setupEmbeddedAdb(): String? {
        return try {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "kswitch_bin")
            tempDir.mkdirs()
            val adbFile = File(tempDir, "adb")

            if (!adbFile.exists()) {
                val input = javaClass.getResourceAsStream("/bin/linux/adb") ?: return null
                Files.copy(input, adbFile.toPath())
                
                // Set execute permission (chmod +x)
                val perms = Files.getPosixFilePermissions(adbFile.toPath()).toMutableSet()
                perms.add(PosixFilePermission.OWNER_EXECUTE)
                Files.setPosixFilePermissions(adbFile.toPath(), perms)
            }
            adbFile.absolutePath
        } catch (e: Exception) {
            null // If setup fails, return null
        }
    }

    suspend fun checkDevices(): List<String> = withContext(Dispatchers.IO) {
        try {
            val output = execute("devices")
            output.lines()
                .drop(1)
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split("\t")
                    if (parts.size >= 2 && parts[1] == "device") parts[0] else null
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun execute(command: String, timeoutSeconds: Long = 60): String = withContext(Dispatchers.IO) {
        val parts = command.split(" ").toTypedArray()
        // Use adbPath instead of "adb"
        runProcess(listOf(adbPath) + parts.toList(), timeoutSeconds)
    }
    
    suspend fun execute(args: List<String>, timeoutSeconds: Long = 60): String = withContext(Dispatchers.IO) {
        // Use adbPath instead of "adb"
        runProcess(listOf(adbPath) + args, timeoutSeconds)
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
