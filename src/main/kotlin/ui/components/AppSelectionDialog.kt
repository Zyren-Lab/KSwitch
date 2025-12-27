package ui.components

import InstalledApp
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AppSelectionDialog(
        apps: List<InstalledApp>,
        selectedPackages: Set<String>,
        onDismiss: () -> Unit,
        onConfirm: (Set<String>) -> Unit
) {
    var tempSelected by remember { mutableStateOf(selectedPackages) }
    val listState = rememberLazyListState()

    // AlertDialog o'rniga oddiy Dialog ishlatamiz
    Dialog(
            onDismissRequest = onDismiss,
            // Dialog oynasining xususiyatlari (shaffof fon yo'q)
            ) {
        // Oynaning tashqi qismi (fon)
        Card(
                shape = RoundedCornerShape(16.dp),
                backgroundColor = AppColors.Surface,
                elevation = 8.dp,
                modifier =
                        Modifier.width(450.dp) // Kengligi
                                .height(600.dp) // Balandligi (Katta va qulay)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // --- SARLAVHA ---
                Text(
                        "Select Apps to Backup",
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                )
                Text("${tempSelected.size} apps selected", color = Color.Gray, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // --- RO'YXAT (Scroll) ---
                Box(
                        modifier =
                                Modifier.weight(1f) // Bor joyni egallaydi
                                        .background(
                                                AppColors.Background.copy(alpha = 0.5f),
                                                RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                                1.dp,
                                                Color.Gray.copy(alpha = 0.2f),
                                                RoundedCornerShape(8.dp)
                                        )
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        items(apps) { app ->
                            val isSelected = app.packageName in tempSelected
                            Row(
                                    modifier =
                                            Modifier.fillMaxWidth()
                                                    .clickable {
                                                        tempSelected =
                                                                if (isSelected)
                                                                        tempSelected -
                                                                                app.packageName
                                                                else tempSelected + app.packageName
                                                    }
                                                    .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            tempSelected =
                                                    if (it) tempSelected + app.packageName
                                                    else tempSelected - app.packageName
                                        },
                                        colors =
                                                CheckboxDefaults.colors(
                                                        checkedColor = AppColors.Primary
                                                )
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                        Icons.Default.Android,
                                        null,
                                        tint = if (isSelected) AppColors.Primary else Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    val name =
                                            app.packageName.substringAfterLast('.')
                                                    .replaceFirstChar { it.uppercase() }
                                    Text(
                                            name,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppColors.TextPrimary
                                    )
                                    Text(
                                            app.packageName,
                                            fontSize = 10.sp,
                                            color = AppColors.TextSecondary
                                    )
                                }
                            }
                            Divider(color = Color.Gray.copy(alpha = 0.1f))
                        }
                    }

                    // Scrollbar
                    VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(listState)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- TUGMALAR ---
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                                checked = tempSelected.size == apps.size,
                                onCheckedChange = {
                                    tempSelected =
                                            if (it) apps.map { app -> app.packageName }.toSet()
                                            else emptySet()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = AppColors.Primary)
                        )
                        Text(
                                "Select All",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AppColors.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                            onClick = { onConfirm(tempSelected) },
                            colors =
                                    ButtonDefaults.buttonColors(
                                            backgroundColor = AppColors.Primary
                                    ),
                            shape = RoundedCornerShape(50)
                    ) { Text("Confirm", color = Color.White) }
                }
            }
        }
    }
}
