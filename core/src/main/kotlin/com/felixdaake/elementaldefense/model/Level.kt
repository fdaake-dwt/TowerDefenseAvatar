package com.felixdaake.elementaldefense.model

import com.badlogic.gdx.math.Vector2

/**
 * One playable track. The path is the polyline raiders walk along, in world units
 * (see [com.felixdaake.elementaldefense.WorldMetrics]). Towers may not be built
 * closer than [pathClearance] to any path segment.
 */
data class Level(
    val id: Int,
    val name: String,
    val description: String,
    val path: List<Vector2>,
    val waves: List<Wave>,
    val startingCurrency: Int,
    val startingLives: Int,
    val pathClearance: Float = 34f
)
