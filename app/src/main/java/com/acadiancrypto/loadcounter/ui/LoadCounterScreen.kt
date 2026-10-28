package com.acadiancrypto.loadcounter.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.acadiancrypto.loadcounter.LoadFormat
import com.acadiancrypto.loadcounter.LoadUiState

@Composable
fun LoadCounterScreen(
    state: LoadUiState,
    onAddLoad: () -> Unit,
    onUndoLast: () -> Unit,
    onResetAll: () -> Unit,
    onExport: () -> Unit,
) {
    // rememberSaveable keeps an open dialog open across configuration changes.
    var confirmUndo by rememberSaveable { mutableStateOf(false) }
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Load Counter", fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.White)

            Text(
                text = state.total.toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = Amber,
            )
            Text("Today: ${state.todayCount}", fontSize = 26.sp, color = Color.White)

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onAddLoad,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 130.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoadGreen, contentColor = Color.White),
                shape = MaterialTheme.shapes.large,
            ) {
                Text("+1 LOAD", fontSize = 44.sp, fontWeight = FontWeight.Black)
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { confirmUndo = true },
                    enabled = state.total > 0,
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                ) { Text("Undo last", fontSize = 20.sp) }
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier.weight(1f).heightIn(min = 64.dp),
                ) { Text("Export CSV", fontSize = 20.sp) }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "Recent loads",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            if (state.loads.isEmpty()) {
                Text("No loads yet", fontSize = 18.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth())
            } else {
                // Last 10, newest first. Load number = position in the full list (1-based).
                state.loads.withIndex().toList().takeLast(10).asReversed().forEach { (index, millis) ->
                    Text(
                        text = "#${index + 1}  ${LoadFormat.displayDateTime(millis)}",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    )
                }
            }

            // Kept far from the main button so it is hard to hit by accident.
            Spacer(Modifier.height(40.dp))
            HorizontalDivider(color = Color.DarkGray)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { confirmReset = true },
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
            ) { Text("Reset all", fontSize = 20.sp) }
        }
    }

    if (confirmUndo) {
        ConfirmDialog(
            message = "Remove the last load?",
            confirmText = "Remove",
            onConfirm = { confirmUndo = false; onUndoLast() },
            onDismiss = { confirmUndo = false },
        )
    }
    if (confirmReset) {
        ConfirmDialog(
            message = "Erase ALL loads? This cannot be undone.",
            confirmText = "Erase all",
            onConfirm = { confirmReset = false; onResetAll() },
            onDismiss = { confirmReset = false },
        )
    }
}

@Composable
private fun ConfirmDialog(message: String, confirmText: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(message, fontSize = 20.sp) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText, fontSize = 18.sp) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", fontSize = 18.sp) } },
    )
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun LoadCounterScreenPreview() {
    val now = System.currentTimeMillis()
    LoadCounterTheme {
        LoadCounterScreen(
            state = LoadUiState(loads = List(12) { now - (12 - it) * 3_600_000L }, todayCount = 5),
            onAddLoad = {}, onUndoLast = {}, onResetAll = {}, onExport = {},
        )
    }
}
