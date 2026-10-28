package com.acadiancrypto.loadcounter

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.acadiancrypto.loadcounter.ui.LoadCounterScreen
import com.acadiancrypto.loadcounter.ui.LoadCounterTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val viewModel: LoadViewModel by viewModels()

    // Opens the system "save file" picker; we get back the chosen file's Uri (or null if cancelled).
    private val exportCsv = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        val csv = LoadFormat.toCsv(viewModel.state.value.loads)
        lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                        ?: error("Could not open file")
                }.isSuccess
            }
            val msg = if (ok) "Export successful" else "Export failed"
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoadCounterTheme {
                val state by viewModel.state.collectAsState()
                LoadCounterScreen(
                    state = state,
                    onAddLoad = {
                        vibrate()
                        viewModel.addLoad()
                    },
                    onUndoLast = viewModel::undoLast,
                    onResetAll = viewModel::resetAll,
                    onExport = { exportCsv.launch(LoadFormat.defaultCsvName()) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    // A real short vibration (rather than view haptics) so it is felt even
    // when the phone's "touch feedback" setting is off.
    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
