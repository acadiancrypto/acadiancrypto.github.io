package com.acadiancrypto.loadcounter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class InMemoryStore : KeyValueStore {
    val map = mutableMapOf<String, String>()
    override fun getString(key: String) = map[key]
    override fun putString(key: String, value: String) { map[key] = value }
    override fun remove(key: String) { map.remove(key) }
}

class LoadRepositoryTest {
    private lateinit var store: InMemoryStore
    private lateinit var repo: LoadRepository

    @Before
    fun setUp() {
        store = InMemoryStore()
        repo = LoadRepository(store)
    }

    @Test
    fun startsEmpty() {
        assertTrue(repo.getLoads().isEmpty())
    }

    @Test
    fun addedLoadsArePersistedInOrder() {
        repo.addLoad(1000L)
        repo.addLoad(2000L)
        // A fresh repository on the same store simulates an app restart.
        assertEquals(listOf(1000L, 2000L), LoadRepository(store).getLoads())
    }

    @Test
    fun removeLastDropsOnlyNewest() {
        repo.addLoad(1000L)
        repo.addLoad(2000L)
        assertEquals(listOf(1000L), repo.removeLast())
        assertEquals(listOf(1000L), repo.getLoads())
    }

    @Test
    fun removeLastOnEmptyIsSafe() {
        assertTrue(repo.removeLast().isEmpty())
    }

    @Test
    fun clearErasesEverything() {
        repo.addLoad(1000L)
        repo.clear()
        assertTrue(repo.getLoads().isEmpty())
    }

    @Test
    fun corruptEntriesAreSkipped() {
        store.putString("loads", "1000,oops,,3000")
        assertEquals(listOf(1000L, 3000L), repo.getLoads())
    }
}
