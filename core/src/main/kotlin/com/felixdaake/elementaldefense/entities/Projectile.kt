package com.felixdaake.elementaldefense.entities

import com.badlogic.gdx.math.Vector2
import com.felixdaake.elementaldefense.model.Element
import com.felixdaake.elementaldefense.model.TowerLevelStats

/**
 * A homing shot fired by a tower. Movement only; hit resolution (damage, splash,
 * status effects) is done by the owning combat system once [update] reports arrival.
 */
class Projectile(
    val position: Vector2,
    val target: Enemy,
    val stats: TowerLevelStats,
    val element: Element
) {
    var alive = true
        private set

    private val scratch = Vector2()

    /** Moves the projectile. Returns true the frame it reaches its target. */
    fun update(delta: Float): Boolean {
        if (!alive) return false
        if (target.isDead || target.reachedEnd) {
            alive = false
            return false
        }

        scratch.set(target.position).sub(position)
        val distance = scratch.len()
        val moveDistance = stats.projectileSpeed * delta

        if (moveDistance >= distance) {
            position.set(target.position)
            alive = false
            return true
        }

        scratch.nor()
        position.mulAdd(scratch, moveDistance)
        return false
    }
}
