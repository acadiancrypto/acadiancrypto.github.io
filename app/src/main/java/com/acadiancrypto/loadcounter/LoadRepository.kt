package com.acadiancrypto.loadcounter

import android.content.Context
import android.content.SharedPreferences

/**
 * Minimal string key/value storage. Production code uses SharedPreferences;
 * unit tests use an in-memory map, so LoadRepository can be tested on a plain JVM.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

class SharedPrefsStore(private val prefs: SharedPreferences) : KeyValueStore {
    constructor(context: Context) :
        this(context.getSharedPreferences("load_counter", Context.MODE_PRIVATE))

    override fun getString(key: String): String? = prefs.getString(key, null)

    // commit() (synchronous) instead of apply() so a load is on disk before we
    // return, even if the app is killed right after the tap. The data is tiny.
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).commit()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).commit()
    }
}

/**
 * Stores every load as epoch milliseconds, oldest first, saved as a
 * comma-separated string, e.g. "1793210000000,1793213600000".
 */
class LoadRepository(private val store: KeyValueStore) {

    fun getLoads(): List<Long> {
        val raw = store.getString(KEY_LOADS) ?: return emptyList()
        // Skip anything unparseable rather than crashing and losing the rest.
        return raw.split(',').mapNotNull { it.trim().toLongOrNull() }
    }

    fun addLoad(timestampMillis: Long): List<Long> {
        val loads = getLoads() + timestampMillis
        save(loads)
        return loads
    }

    /** Removes the most recent load. Returns the remaining loads. */
    fun removeLast(): List<Long> {
        val loads = getLoads().dropLast(1)
        save(loads)
        return loads
    }

    fun clear() {
        store.remove(KEY_LOADS)
    }

    private fun save(loads: List<Long>) {
        if (loads.isEmpty()) store.remove(KEY_LOADS)
        else store.putString(KEY_LOADS, loads.joinToString(","))
    }

    companion object {
        private const val KEY_LOADS = "loads"
    }
}
