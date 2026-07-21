package com.felixdaake.elementaldefense.model

/** One group of enemies spawned back to back within a wave. */
data class SpawnEntry(
    val enemyType: EnemyType,
    val count: Int,
    val interval: Float,
    val hpMultiplier: Float = 1f
)

/** A full wave: several spawn groups, followed by a breather before the next wave. */
data class Wave(
    val entries: List<SpawnEntry>,
    val delayAfter: Float = 4f
)
