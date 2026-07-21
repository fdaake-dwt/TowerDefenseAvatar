package com.felixdaake.elementaldefense.entities

import com.badlogic.gdx.math.Vector2
import com.felixdaake.elementaldefense.model.TowerLevelStats
import com.felixdaake.elementaldefense.model.TowerType

/** A placed tower. Tier 0 is the freshly-built tower; higher tiers come from upgrades. */
class Tower(
    val type: TowerType,
    val position: Vector2
) {
    var tier: Int = 0
        private set

    var cooldown: Float = 0f

    val stats: TowerLevelStats get() = type.levels[tier]
    val canUpgrade: Boolean get() = tier < type.maxTier
    fun nextUpgradeCost(): Int = type.levels[tier + 1].cost

    fun upgrade() {
        if (canUpgrade) tier++
    }
}
