package com.felixdaake.elementaldefense.model

/**
 * Stats for a single tier of a tower. `cost` on tier 0 is the build price;
 * on later tiers it is the additional gold needed to upgrade into that tier.
 */
data class TowerLevelStats(
    val cost: Int,
    val damage: Float,
    val range: Float,
    val attackInterval: Float,
    val projectileSpeed: Float,
    val splashRadius: Float = 0f,
    val pierceCount: Int = 1,
    val slowFactor: Float = 1f,
    val slowDuration: Float = 0f,
    val burnDamagePerSecond: Float = 0f,
    val burnDuration: Float = 0f
)

/**
 * The four buildable tower categories, one per bending element.
 * Each has a small upgrade path (tier 0 = base build, tier 1 = upgrade).
 */
enum class TowerType(val displayName: String, val element: Element, val levels: List<TowerLevelStats>) {
    FIRE_TOWER(
        displayName = "Feuerbändiger",
        element = Element.FEUER,
        levels = listOf(
            TowerLevelStats(
                cost = 50, damage = 8f, range = 110f, attackInterval = 0.9f, projectileSpeed = 260f,
                burnDamagePerSecond = 4f, burnDuration = 3f
            ),
            TowerLevelStats(
                cost = 60, damage = 14f, range = 125f, attackInterval = 0.8f, projectileSpeed = 280f,
                burnDamagePerSecond = 7f, burnDuration = 3.5f
            )
        )
    ),
    WATER_TOWER(
        displayName = "Wasserbändiger",
        element = Element.WASSER,
        levels = listOf(
            TowerLevelStats(
                cost = 45, damage = 5f, range = 120f, attackInterval = 1.0f, projectileSpeed = 280f,
                splashRadius = 40f, slowFactor = 0.6f, slowDuration = 1.5f
            ),
            TowerLevelStats(
                cost = 55, damage = 8f, range = 135f, attackInterval = 0.9f, projectileSpeed = 300f,
                splashRadius = 50f, slowFactor = 0.45f, slowDuration = 2.0f
            )
        )
    ),
    EARTH_TOWER(
        displayName = "Erdbändiger",
        element = Element.ERDE,
        levels = listOf(
            TowerLevelStats(
                cost = 60, damage = 22f, range = 95f, attackInterval = 1.6f, projectileSpeed = 200f,
                splashRadius = 30f
            ),
            TowerLevelStats(
                cost = 70, damage = 36f, range = 105f, attackInterval = 1.4f, projectileSpeed = 220f,
                splashRadius = 40f
            )
        )
    ),
    AIR_TOWER(
        displayName = "Luftbändiger",
        element = Element.LUFT,
        levels = listOf(
            TowerLevelStats(
                cost = 55, damage = 6f, range = 130f, attackInterval = 0.5f, projectileSpeed = 320f,
                pierceCount = 2
            ),
            TowerLevelStats(
                cost = 65, damage = 9f, range = 145f, attackInterval = 0.4f, projectileSpeed = 340f,
                pierceCount = 3
            )
        )
    );

    val maxTier: Int get() = levels.size - 1
}
