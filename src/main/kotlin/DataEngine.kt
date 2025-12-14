
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * DataEngine - Handles Contacts and Call Logs
 * Uses content query for reading system data
 */
object DataEngine {
    
    /**
     * Backup Contacts to VCF format (Experimental)
     * Queries content://com.android.contacts/data
     */
    suspend fun backupContacts(backupRoot: File): Int = withContext(Dispatchers.IO) {
        val contactsDir = File(backupRoot, "Contacts")
        contactsDir.mkdirs()
        
        val contacts = mutableListOf<ContactEntry>()
        
        try {
            // Query contacts data
            val cmd = listOf(
                "shell", "content", "query",
                "--uri", "content://com.android.contacts/data",
                "--projection", "display_name:data1:mimetype"
            )
            
            val output = AdbClient.execute(cmd, timeoutSeconds = 60)
            
            var currentName = ""
            var currentPhone = ""
            
            output.lines().forEach { line ->
                if (line.contains("display_name=")) {
                    val name = extractValue(line, "display_name")
                    val data = extractValue(line, "data1")
                    val mime = extractValue(line, "mimetype")
                    
                    when {
                        mime.contains("name") && name.isNotEmpty() -> currentName = name
                        mime.contains("phone") && data.isNotEmpty() -> {
                            currentPhone = data
                            if (currentName.isNotEmpty()) {
                                contacts.add(ContactEntry(currentName, currentPhone))
                            }
                        }
                    }
                }
            }
            
            // Save to VCF
            if (contacts.isNotEmpty()) {
                val vcfFile = File(contactsDir, "contacts.vcf")
                val vcfContent = buildString {
                    contacts.distinctBy { it.name + it.phone }.forEach { contact ->
                        appendLine("BEGIN:VCARD")
                        appendLine("VERSION:3.0")
                        appendLine("FN:${contact.name}")
                        appendLine("TEL:${contact.phone}")
                        appendLine("END:VCARD")
                    }
                }
                vcfFile.writeText(vcfContent)
            }
            
        } catch (e: Exception) {
            println("Contacts backup error: ${e.message}")
        }
        
        contacts.size
    }
    
    /**
     * Backup Call Logs to JSON (Backup Only)
     * Queries content://call_log/calls
     */
    suspend fun backupCallLogs(backupRoot: File): Int = withContext(Dispatchers.IO) {
        val callLogDir = File(backupRoot, "CallLog")
        callLogDir.mkdirs()
        
        val calls = mutableListOf<CallLogEntry>()
        
        try {
            val cmd = listOf(
                "shell", "content", "query",
                "--uri", "content://call_log/calls",
                "--projection", "number:date:duration:type"
            )
            
            val output = AdbClient.execute(cmd, timeoutSeconds = 60)
            
            output.lines().forEach { line ->
                if (line.contains("number=")) {
                    val number = extractValue(line, "number")
                    val date = extractValue(line, "date")
                    val duration = extractValue(line, "duration")
                    val type = extractValue(line, "type")
                    
                    if (number.isNotEmpty()) {
                        calls.add(CallLogEntry(number, date, duration, type))
                    }
                }
            }
            
            // Save as JSON
            if (calls.isNotEmpty()) {
                val jsonFile = File(callLogDir, "call_logs.json")
                val jsonContent = buildString {
                    appendLine("[")
                    calls.forEachIndexed { index, call ->
                        val typeStr = when(call.type) {
                            "1" -> "incoming"
                            "2" -> "outgoing"
                            "3" -> "missed"
                            else -> "unknown"
                        }
                        append("""  {"number": "${call.number}", "date": "${call.date}", "duration": "${call.duration}", "type": "$typeStr"}""")
                        if (index < calls.size - 1) appendLine(",") else appendLine()
                    }
                    appendLine("]")
                }
                jsonFile.writeText(jsonContent)
            }
            
        } catch (e: Exception) {
            println("Call log backup error: ${e.message}")
        }
        
        calls.size
    }
    
    private fun extractValue(line: String, key: String): String {
        val pattern = "$key="
        val startIndex = line.indexOf(pattern)
        if (startIndex == -1) return ""
        
        val valueStart = startIndex + pattern.length
        val commaIndex = line.indexOf(",", valueStart)
        
        return if (commaIndex != -1) {
            line.substring(valueStart, commaIndex).trim()
        } else {
            line.substring(valueStart).trim()
        }
    }
}
