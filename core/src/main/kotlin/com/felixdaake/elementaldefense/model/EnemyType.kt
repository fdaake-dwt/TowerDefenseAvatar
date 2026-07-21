package com.felixdaake.elementaldefense.model

import com.badlogic.gdx.graphics.Color

/**
 * Static stats for one kind of raider. Instances of [com.felixdaake.elementaldefense.entities.Enemy]
 * reference one of these for their base values; wave scaling is applied on top at spawn time.
 */
enum class EnemyType(
    val displayName: String,
    val baseHp: Float,
    val speed: Float, // world pixels per second along the path
    val reward: Int, // currency granted on kill
    val livesLost: Int, // village lives lost if it reaches the end
    val color: Color,
    val radius: Float,
    val resistantTo: Element?, // takes half damage from this element
    val weakTo: Element? // takes 1.6x damage from this element
) {
    GRUNT_RAIDER(
        displayName = "Räuber",
        baseHp = 40f,
        speed = 70f,
        reward = 5,
        livesLost = 1,
        color = Color(0.55f, 0.50f, 0.45f, 1f),
        radius = 10f,
        resistantTo = null,
        weakTo = null
    ),
    ICY_RAIDER(
        displayName = "Eisiger Räuber",
        baseHp = 55f,
        speed = 55f,
        reward = 7,
        livesLost = 1,
        color = Color(0.65f, 0.85f, 1.0f, 1f),
        radius = 11f,
        resistantTo = Element.WASSER,
        weakTo = Element.FEUER
    ),
    PLANT_RAIDER(
        displayName = "Pflanzen-Avatar",
        baseHp = 70f,
        speed = 48f,
        reward = 8,
        livesLost = 1,
        color = Color(0.25f, 0.65f, 0.30f, 1f),
        radius = 12f,
        resistantTo = Element.ERDE,
        weakTo = Element.LUFT
    ),
    SCOUT_RAIDER(
        displayName = "Kundschafter",
        baseHp = 25f,
        speed = 125f,
        reward = 6,
        livesLost = 1,
        color = Color(0.95f, 0.85f, 0.25f, 1f),
        radius = 8f,
        resistantTo = Element.LUFT,
        weakTo = Element.ERDE
    );

    fun damageMultiplierFor(source: Element): Float = when (source) {
        resistantTo -> 0.5f
        weakTo -> 1.6f
        else -> 1.0f
    }
}
