package com.felixdaake.elementaldefense.entities

import com.badlogic.gdx.math.Vector2
import com.felixdaake.elementaldefense.model.Element
import com.felixdaake.elementaldefense.model.EnemyType

/** A raider walking along a level's path. */
class Enemy(
    val type: EnemyType,
    private val path: List<Vector2>,
    hpMultiplier: Float
) {
    val maxHp: Float = type.baseHp * hpMultiplier
    var hp: Float = maxHp
        private set

    val position = Vector2(path[0])
    private var segmentIndex = 0
    private var distanceIntoSegment = 0f

    var slowTimeRemaining = 0f
    private var slowFactor = 1f
    var burnTimeRemaining = 0f
    private var burnDps = 0f

    val isDead: Boolean get() = hp <= 0f
    var reachedEnd = false
        private set

    /** Advances the enemy along the path and applies any active status effects. Returns true if it just died. */
    fun update(delta: Float): Boolean {
        if (isDead || reachedEnd) return false

        if (burnTimeRemaining > 0f) {
            hp -= burnDps * delta
            burnTimeRemaining -= delta
            if (hp <= 0f) return true
        }

        val effectiveSpeed = if (slowTimeRemaining > 0f) {
            slowTimeRemaining -= delta
            type.speed * slowFactor
        } else {
            type.speed
        }

        var remaining = effectiveSpeed * delta
        while (remaining > 0f && segmentIndex < path.size - 1) {
            val start = path[segmentIndex]
            val end = path[segmentIndex + 1]
            val segmentLength = start.dst(end)
            val leftInSegment = segmentLength - distanceIntoSegment
            if (remaining < leftInSegment) {
                distanceIntoSegment += remaining
                position.set(start).lerp(end, distanceIntoSegment / segmentLength)
                remaining = 0f
            } else {
                remaining -= leftInSegment
                segmentIndex++
                distanceIntoSegment = 0f
                if (segmentIndex >= path.size - 1) {
                    position.set(path.last())
                    reachedEnd = true
                } else {
                    position.set(path[segmentIndex])
                }
            }
        }
        return false
    }

    fun applyDamage(amount: Float, source: Element) {
        hp -= amount * type.damageMultiplierFor(source)
    }

    fun applySlow(factor: Float, duration: Float) {
        if (duration > slowTimeRemaining) {
            slowFactor = factor
            slowTimeRemaining = duration
        }
    }

    fun applyBurn(damagePerSecond: Float, duration: Float) {
        if (damagePerSecond > 0f) {
            burnDps = damagePerSecond
            burnTimeRemaining = duration
        }
    }

    /** Fraction of the path already covered, used for tower targeting priorities. */
    fun pathProgress(): Float = segmentIndex.toFloat()
}
