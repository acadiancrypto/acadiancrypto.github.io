package com.acadiancrypto.loadcounter

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

data class LoadUiState(
    /** All loads, oldest first, as epoch milliseconds. */
    val loads: List<Long> = emptyList(),
    val todayCount: Int = 0,
) {
    val total: Int get() = loads.size
}

class LoadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LoadRepository(SharedPrefsStore(application))

    private val _state = MutableStateFlow(buildState(repository.getLoads()))
    val state: StateFlow<LoadUiState> = _state.asStateFlow()

    fun addLoad() {
        _state.value = buildState(repository.addLoad(System.currentTimeMillis()))
    }

    fun undoLast() {
        _state.value = buildState(repository.removeLast())
    }

    fun resetAll() {
        repository.clear()
        _state.value = buildState(emptyList())
    }

    /** Called on resume so "Today" rolls over correctly after midnight. */
    fun refresh() {
        _state.value = buildState(repository.getLoads())
    }

    private fun buildState(loads: List<Long>) =
        LoadUiState(loads = loads, todayCount = LoadFormat.countOnDate(loads, LocalDate.now()))
}
