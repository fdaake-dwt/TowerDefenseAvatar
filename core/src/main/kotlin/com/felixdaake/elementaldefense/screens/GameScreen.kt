package com.felixdaake.elementaldefense.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.felixdaake.elementaldefense.ElementalDefenseGame
import com.felixdaake.elementaldefense.WorldMetrics
import com.felixdaake.elementaldefense.entities.Tower
import com.felixdaake.elementaldefense.logic.GameState
import com.felixdaake.elementaldefense.model.Level
import com.felixdaake.elementaldefense.model.Levels
import com.felixdaake.elementaldefense.model.TowerType

/** The actual tower-defense gameplay for one level. */
class GameScreen(private val game: ElementalDefenseGame, private val level: Level) : ScreenAdapter() {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(WorldMetrics.WIDTH, WorldMetrics.HEIGHT, camera)
    private val touchPoint = Vector3()

    private val state = GameState(level)

    private var selectedTowerType: TowerType? = null
    private var selectedTower: Tower? = null
    private var unlockedNextLevel = false

    // --- static UI layout -------------------------------------------------
    private val topBarY = WorldMetrics.HEIGHT - 50f
    private val backButton = Rectangle(10f, topBarY + 6f, 90f, 38f)
    private val startWaveButton = Rectangle(WorldMetrics.WIDTH - 180f, topBarY + 6f, 170f, 38f)

    private val shopButtonWidth = 200f
    private val shopButtons: List<Pair<TowerType, Rectangle>> = TowerType.entries.mapIndexed { index, type ->
        val gap = 20f
        val totalWidth = TowerType.entries.size * (shopButtonWidth + gap) - gap
        val startX = (WorldMetrics.WIDTH - totalWidth) / 2f
        type to Rectangle(startX + index * (shopButtonWidth + gap), 10f, shopButtonWidth, 70f)
    }

    private val playfieldBottom = 96f
    private val playfieldTop = topBarY - 6f

    private val hasNextLevel = level.id + 1 < Levels.all.size

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (!state.gameOver) {
            state.update(delta)
        }
        if (state.victory && !unlockedNextLevel) {
            unlockedNextLevel = true
            if (level.id + 1 < Levels.all.size) {
                game.saveData.unlockUpTo(level.id + 1)
            }
        }
        handleInput()
        draw()
    }

    // ------------------------------------------------------------------ draw

    private fun draw() {
        Gdx.gl.glClearColor(0.20f, 0.38f, 0.18f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()

        val shapes = game.shapes
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)

        drawPath(shapes)
        drawRangeIndicators(shapes)
        drawTowers(shapes)
        drawEnemies(shapes)
        drawProjectiles(shapes)
        drawTopBar(shapes)
        drawShopBar(shapes)
        drawSelectedTowerPanel(shapes)
        if (state.gameOver) drawOverlay(shapes)

        shapes.end()

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        drawHudText()
        drawShopLabels()
        drawSelectedTowerLabels()
        if (state.gameOver) drawOverlayText()
        game.batch.end()
    }

    private fun drawPath(shapes: ShapeRenderer) {
        shapes.color = Color(0.45f, 0.36f, 0.22f, 1f)
        for (i in 0 until level.path.size - 1) {
            shapes.rectLine(level.path[i], level.path[i + 1], 34f)
        }
    }

    private fun drawRangeIndicators(shapes: ShapeRenderer) {
        selectedTower?.let {
            shapes.color = Color(1f, 1f, 1f, 0.15f)
            shapes.circle(it.position.x, it.position.y, it.stats.range, 40)
        }
    }

    private fun drawTowers(shapes: ShapeRenderer) {
        for (tower in state.towers) {
            shapes.color = tower.type.element.color
            shapes.circle(tower.position.x, tower.position.y, 18f + tower.tier * 4f, 24)
            shapes.color = if (tower === selectedTower) Color.WHITE else Color(0f, 0f, 0f, 0.6f)
            shapes.circle(tower.position.x, tower.position.y, 18f + tower.tier * 4f, 24)
            shapes.color = tower.type.element.color
            shapes.circle(tower.position.x, tower.position.y, 14f + tower.tier * 3f, 24)
        }
    }

    private fun drawEnemies(shapes: ShapeRenderer) {
        for (enemy in state.enemies) {
            shapes.color = enemy.type.color
            shapes.circle(enemy.position.x, enemy.position.y, enemy.type.radius, 16)
            // hp bar
            val barWidth = enemy.type.radius * 2f
            val hpFraction = (enemy.hp / enemy.maxHp).coerceIn(0f, 1f)
            shapes.color = Color(0.15f, 0.15f, 0.15f, 1f)
            shapes.rect(enemy.position.x - barWidth / 2f, enemy.position.y + enemy.type.radius + 4f, barWidth, 4f)
            shapes.color = Color(0.85f, 0.15f, 0.15f, 1f)
            shapes.rect(enemy.position.x - barWidth / 2f, enemy.position.y + enemy.type.radius + 4f, barWidth * hpFraction, 4f)
        }
    }

    private fun drawProjectiles(shapes: ShapeRenderer) {
        for (projectile in state.projectiles) {
            shapes.color = projectile.element.color
            shapes.circle(projectile.position.x, projectile.position.y, 5f, 10)
        }
    }

    private fun drawTopBar(shapes: ShapeRenderer) {
        shapes.color = Color(0.10f, 0.12f, 0.16f, 0.92f)
        shapes.rect(0f, topBarY, WorldMetrics.WIDTH, 50f)
        shapes.color = Color(0.20f, 0.24f, 0.30f, 1f)
        shapes.rect(backButton.x, backButton.y, backButton.width, backButton.height)
        val canStart = !state.gameOver && state.waveManager.canStartNextWave()
        shapes.color = if (canStart) Color(0.20f, 0.55f, 0.30f, 1f) else Color(0.25f, 0.25f, 0.25f, 1f)
        shapes.rect(startWaveButton.x, startWaveButton.y, startWaveButton.width, startWaveButton.height)
    }

    private fun drawShopBar(shapes: ShapeRenderer) {
        shapes.color = Color(0.10f, 0.12f, 0.16f, 0.92f)
        shapes.rect(0f, 0f, WorldMetrics.WIDTH, 90f)
        for ((type, rect) in shopButtons) {
            val affordable = state.currency >= type.levels[0].cost
            shapes.color = when {
                type == selectedTowerType -> Color(0.95f, 0.85f, 0.25f, 1f)
                !affordable -> Color(0.22f, 0.22f, 0.24f, 1f)
                else -> Color(0.28f, 0.30f, 0.34f, 1f)
            }
            shapes.rect(rect.x, rect.y, rect.width, rect.height)
            shapes.color = type.element.color
            shapes.circle(rect.x + 26f, rect.y + rect.height / 2f, 14f, 20)
        }
    }

    private fun drawSelectedTowerPanel(shapes: ShapeRenderer) {
        val tower = selectedTower ?: return
        val (upgradeRect, sellRect) = towerPanelRects(tower)
        if (tower.canUpgrade) {
            val affordable = state.currency >= tower.nextUpgradeCost()
            shapes.color = if (affordable) Color(0.20f, 0.55f, 0.30f, 1f) else Color(0.3f, 0.3f, 0.3f, 1f)
            shapes.rect(upgradeRect.x, upgradeRect.y, upgradeRect.width, upgradeRect.height)
        }
        shapes.color = Color(0.65f, 0.20f, 0.20f, 1f)
        shapes.rect(sellRect.x, sellRect.y, sellRect.width, sellRect.height)
    }

    private fun drawOverlay(shapes: ShapeRenderer) {
        shapes.color = Color(0f, 0f, 0f, 0.65f)
        shapes.rect(0f, 0f, WorldMetrics.WIDTH, WorldMetrics.HEIGHT)
        val (primary, menu) = overlayButtonRects()
        shapes.color = Color(0.20f, 0.55f, 0.30f, 1f)
        shapes.rect(primary.x, primary.y, primary.width, primary.height)
        shapes.color = Color(0.30f, 0.32f, 0.36f, 1f)
        shapes.rect(menu.x, menu.y, menu.width, menu.height)
    }

    // ------------------------------------------------------------- draw text

    private fun drawHudText() {
        game.font.color = Color.WHITE
        game.font.data.setScale(1.1f)
        game.font.draw(game.batch, "Zurueck", backButton.x, backButton.y + backButton.height / 2f + 8f, backButton.width, Align.center, false)
        game.font.draw(
            game.batch,
            "Gold: ${state.currency}   Leben: ${state.lives}   Welle: ${state.waveManager.currentWaveIndex + 1}/${state.waveManager.totalWaves}",
            110f, topBarY + 34f
        )
        val startLabel = if (state.waveManager.canStartNextWave()) "Naechste Welle" else "Welle laeuft..."
        game.font.draw(game.batch, startLabel, startWaveButton.x, startWaveButton.y + startWaveButton.height / 2f + 8f, startWaveButton.width, Align.center, false)
    }

    private fun drawShopLabels() {
        game.font.data.setScale(1.0f)
        for ((type, rect) in shopButtons) {
            game.font.color = if (state.currency >= type.levels[0].cost) Color.WHITE else Color(0.6f, 0.6f, 0.6f, 1f)
            game.font.draw(game.batch, type.displayName, rect.x + 50f, rect.y + rect.height - 14f)
            game.font.draw(game.batch, "${type.levels[0].cost} Gold", rect.x + 50f, rect.y + rect.height - 40f)
        }
    }

    private fun drawSelectedTowerLabels() {
        val tower = selectedTower ?: return
        val (upgradeRect, sellRect) = towerPanelRects(tower)
        game.font.data.setScale(0.9f)
        if (tower.canUpgrade) {
            game.font.color = Color.WHITE
            game.font.draw(game.batch, "Upgrade (${tower.nextUpgradeCost()})", upgradeRect.x, upgradeRect.y + upgradeRect.height / 2f + 6f, upgradeRect.width, Align.center, false)
        }
        game.font.color = Color.WHITE
        game.font.draw(game.batch, "Verkaufen", sellRect.x, sellRect.y + sellRect.height / 2f + 6f, sellRect.width, Align.center, false)
    }

    private fun drawOverlayText() {
        val (primary, menu) = overlayButtonRects()
        game.font.data.setScale(1.8f)
        game.font.color = Color.WHITE
        val title = if (state.victory) "Sieg!" else "Niederlage"
        game.font.draw(game.batch, title, 0f, WorldMetrics.HEIGHT / 2f + 90f, WorldMetrics.WIDTH, Align.center, false)

        game.font.data.setScale(1.2f)
        val primaryLabel = if (state.victory && hasNextLevel) "Naechstes Level" else "Nochmal"
        game.font.draw(game.batch, primaryLabel, primary.x, primary.y + primary.height / 2f + 8f, primary.width, Align.center, false)
        game.font.draw(game.batch, "Menue", menu.x, menu.y + menu.height / 2f + 8f, menu.width, Align.center, false)
    }

    // ----------------------------------------------------------------- input

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return
        touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(touchPoint)
        val tx = touchPoint.x
        val ty = touchPoint.y

        if (state.gameOver) {
            handleOverlayTouch(tx, ty)
            return
        }

        if (backButton.contains(tx, ty)) {
            game.setScreen(LevelSelectScreen(game))
            return
        }
        if (startWaveButton.contains(tx, ty)) {
            state.startNextWave()
            return
        }
        for ((type, rect) in shopButtons) {
            if (rect.contains(tx, ty)) {
                selectedTower = null
                selectedTowerType = if (selectedTowerType == type) null else type
                return
            }
        }

        selectedTower?.let { tower ->
            val (upgradeRect, sellRect) = towerPanelRects(tower)
            if (tower.canUpgrade && upgradeRect.contains(tx, ty)) {
                state.upgradeTower(tower)
                return
            }
            if (sellRect.contains(tx, ty)) {
                state.sellTower(tower)
                selectedTower = null
                return
            }
        }

        val typeToPlace = selectedTowerType
        if (typeToPlace != null) {
            if (ty in playfieldBottom..playfieldTop) {
                if (state.placeTower(typeToPlace, Vector2(tx, ty))) {
                    selectedTowerType = null
                }
            }
            return
        }

        val hit = state.towers.firstOrNull { it.position.dst(tx, ty) <= 22f }
        selectedTower = hit
    }

    private fun handleOverlayTouch(tx: Float, ty: Float) {
        val (primary, menu) = overlayButtonRects()
        if (primary.contains(tx, ty)) {
            if (state.victory && hasNextLevel) {
                game.setScreen(GameScreen(game, Levels.byId(level.id + 1)))
            } else {
                game.setScreen(GameScreen(game, level))
            }
        } else if (menu.contains(tx, ty)) {
            game.setScreen(LevelSelectScreen(game))
        }
    }

    // --------------------------------------------------------------- layout

    private fun towerPanelRects(tower: Tower): Pair<Rectangle, Rectangle> {
        val px = tower.position.x
        val py = (tower.position.y + 30f).coerceIn(playfieldBottom + 20f, playfieldTop - 20f)
        return Rectangle(px - 105f, py, 100f, 36f) to Rectangle(px + 5f, py, 100f, 36f)
    }

    private fun overlayButtonRects(): Pair<Rectangle, Rectangle> {
        val primary = Rectangle(WorldMetrics.WIDTH / 2f - 220f, WorldMetrics.HEIGHT / 2f - 40f, 200f, 56f)
        val menu = Rectangle(WorldMetrics.WIDTH / 2f + 20f, WorldMetrics.HEIGHT / 2f - 40f, 200f, 56f)
        return primary to menu
    }
}
