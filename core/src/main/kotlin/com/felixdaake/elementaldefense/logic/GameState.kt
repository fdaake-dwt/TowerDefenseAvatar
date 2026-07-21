package com.felixdaake.elementaldefense.logic

import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.felixdaake.elementaldefense.entities.Enemy
import com.felixdaake.elementaldefense.entities.Projectile
import com.felixdaake.elementaldefense.entities.Tower
import com.felixdaake.elementaldefense.model.Level
import com.felixdaake.elementaldefense.model.TowerType

/**
 * The whole live simulation for one playthrough of a level: enemies, towers,
 * projectiles, economy and win/lose state. Free of any rendering/LibGDX-Screen
 * concerns so it can be driven from a fixed-timestep loop and tested on its own.
 */
class GameState(val level: Level) {

    val enemies = mutableListOf<Enemy>()
    val towers = mutableListOf<Tower>()
    val projectiles = mutableListOf<Projectile>()

    var currency: Int = level.startingCurrency
        private set
    var lives: Int = level.startingLives
        private set

    var gameOver = false
        private set
    var victory = false
        private set

    val waveManager = WaveManager(level) { enemies.add(it) }

    fun update(delta: Float) {
        if (gameOver) return
        waveManager.update(delta)
        updateTowers(delta)
        updateProjectiles(delta)
        updateEnemies(delta)
        checkVictory()
    }

    fun canPlaceTower(type: TowerType, pos: Vector2): Boolean {
        val cost = type.levels[0].cost
        if (currency < cost) return false
        if (!isFarEnoughFromPath(pos, level.pathClearance)) return false
        if (towers.any { it.position.dst(pos) < MIN_TOWER_SPACING }) return false
        return true
    }

    fun placeTower(type: TowerType, pos: Vector2): Boolean {
        if (!canPlaceTower(type, pos)) return false
        currency -= type.levels[0].cost
        towers.add(Tower(type, Vector2(pos)))
        return true
    }

    fun upgradeTower(tower: Tower): Boolean {
        if (!tower.canUpgrade) return false
        val cost = tower.nextUpgradeCost()
        if (currency < cost) return false
        currency -= cost
        tower.upgrade()
        return true
    }

    fun startNextWave() = waveManager.startNextWave()

    /** Removes the tower and refunds part of what was spent on it. Returns the refund amount. */
    fun sellTower(tower: Tower): Int {
        val invested = (0..tower.tier).sumOf { tower.type.levels[it].cost }
        val refund = (invested * SELL_REFUND_FRACTION).toInt()
        towers.remove(tower)
        currency += refund
        return refund
    }

    private fun updateTowers(delta: Float) {
        for (tower in towers) {
            tower.cooldown -= delta
            if (tower.cooldown > 0f) continue
            val target = findTarget(tower) ?: continue
            projectiles.add(Projectile(Vector2(tower.position), target, tower.stats, tower.type.element))
            tower.cooldown = tower.stats.attackInterval
        }
    }

    private fun findTarget(tower: Tower): Enemy? =
        enemies.filter { it.position.dst(tower.position) <= tower.stats.range }
            .maxByOrNull { it.pathProgress() }

    private fun updateProjectiles(delta: Float) {
        val iterator = projectiles.iterator()
        while (iterator.hasNext()) {
            val projectile = iterator.next()
            val hit = projectile.update(delta)
            if (hit) {
                resolveHit(projectile)
                iterator.remove()
            } else if (!projectile.alive) {
                // target died or reached the end mid-flight; drop the shot without an effect
                iterator.remove()
            }
        }
    }

    private fun resolveHit(projectile: Projectile) {
        val stats = projectile.stats
        val primary = projectile.target
        applyHit(primary, projectile)

        if (stats.splashRadius > 0f) {
            for (enemy in enemies) {
                if (enemy !== primary && enemy.position.dst(projectile.position) <= stats.splashRadius) {
                    applyHit(enemy, projectile)
                }
            }
        } else if (stats.pierceCount > 1) {
            enemies.asSequence()
                .filter { it !== primary && it.position.dst(projectile.position) <= GUST_RADIUS }
                .sortedBy { it.position.dst(projectile.position) }
                .take(stats.pierceCount - 1)
                .forEach { applyHit(it, projectile) }
        }
    }

    private fun applyHit(enemy: Enemy, projectile: Projectile) {
        enemy.applyDamage(projectile.stats.damage, projectile.element)
        if (projectile.stats.slowFactor < 1f) {
            enemy.applySlow(projectile.stats.slowFactor, projectile.stats.slowDuration)
        }
        if (projectile.stats.burnDamagePerSecond > 0f) {
            enemy.applyBurn(projectile.stats.burnDamagePerSecond, projectile.stats.burnDuration)
        }
    }

    private fun updateEnemies(delta: Float) {
        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.update(delta)
            when {
                enemy.isDead -> {
                    currency += enemy.type.reward
                    iterator.remove()
                }
                enemy.reachedEnd -> {
                    lives = (lives - enemy.type.livesLost).coerceAtLeast(0)
                    iterator.remove()
                    if (lives <= 0) gameOver = true
                }
            }
        }
    }

    private fun checkVictory() {
        if (!victory && waveManager.allWavesStarted && enemies.isEmpty()) {
            victory = true
            gameOver = true
        }
    }

    private fun isFarEnoughFromPath(pos: Vector2, clearance: Float): Boolean {
        for (i in 0 until level.path.size - 1) {
            val distance = Intersector.distanceSegmentPoint(level.path[i], level.path[i + 1], pos)
            if (distance < clearance) return false
        }
        return true
    }

    companion object {
        const val MIN_TOWER_SPACING = 40f
        const val GUST_RADIUS = 50f
        const val SELL_REFUND_FRACTION = 0.6
    }
}
