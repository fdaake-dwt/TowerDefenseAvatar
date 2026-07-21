package com.felixdaake.elementaldefense.logic

import com.felixdaake.elementaldefense.entities.Enemy
import com.felixdaake.elementaldefense.model.Level
import com.felixdaake.elementaldefense.model.SpawnEntry

/**
 * Drives wave spawning for one level. Waves are started manually by the player
 * (like the "Start Round" button in Bloons TD), but if the player doesn't start
 * the next one within [Wave.delayAfter] seconds of the previous wave finishing
 * spawning, it starts automatically so the game never stalls.
 */
class WaveManager(
    private val level: Level,
    private val onSpawn: (Enemy) -> Unit
) {
    var currentWaveIndex = -1
        private set

    var spawningComplete = true
        private set

    private var autoStartTimer = -1f
    private var pendingEntries: MutableList<PendingEntry> = mutableListOf()

    val totalWaves: Int get() = level.waves.size
    val allWavesStarted: Boolean get() = currentWaveIndex >= level.waves.size - 1 && spawningComplete

    private class PendingEntry(val entry: SpawnEntry, var remaining: Int, var timer: Float)

    fun update(delta: Float) {
        if (!spawningComplete) {
            spawnTick(delta)
        } else if (autoStartTimer >= 0f) {
            autoStartTimer -= delta
            if (autoStartTimer <= 0f) startNextWave()
        }
    }

    fun canStartNextWave(): Boolean = spawningComplete && currentWaveIndex < level.waves.size - 1

    fun startNextWave() {
        if (!canStartNextWave()) return
        currentWaveIndex++
        val wave = level.waves[currentWaveIndex]
        pendingEntries = wave.entries.map { PendingEntry(it, it.count, 0f) }.toMutableList()
        spawningComplete = false
        autoStartTimer = -1f
    }

    private fun spawnTick(delta: Float) {
        var stillSpawning = false
        for (pending in pendingEntries) {
            if (pending.remaining <= 0) continue
            stillSpawning = true
            pending.timer -= delta
            if (pending.timer <= 0f) {
                onSpawn(Enemy(pending.entry.enemyType, level.path, pending.entry.hpMultiplier))
                pending.remaining--
                pending.timer = pending.entry.interval
            }
        }
        if (!stillSpawning) {
            spawningComplete = true
            val wave = level.waves[currentWaveIndex]
            autoStartTimer = if (currentWaveIndex < level.waves.size - 1) wave.delayAfter else -1f
        }
    }
}
