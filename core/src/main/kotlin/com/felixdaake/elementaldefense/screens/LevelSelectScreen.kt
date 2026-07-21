package com.felixdaake.elementaldefense.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.felixdaake.elementaldefense.ElementalDefenseGame
import com.felixdaake.elementaldefense.WorldMetrics
import com.felixdaake.elementaldefense.model.Level
import com.felixdaake.elementaldefense.model.Levels

/**
 * Track selection, Mario-Kart-Cup style: every level is shown, but locked ones
 * cannot be entered until the previous one has been won.
 */
class LevelSelectScreen(private val game: ElementalDefenseGame) : ScreenAdapter() {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(WorldMetrics.WIDTH, WorldMetrics.HEIGHT, camera)
    private val touchPoint = Vector3()

    private val backButton = Rectangle(20f, WorldMetrics.HEIGHT - 60f, 120f, 44f)
    private val cardWidth = 720f
    private val cardHeight = 110f
    private val cardGap = 24f

    private val cards: List<Pair<Level, Rectangle>> = Levels.all.mapIndexed { index, level ->
        val top = WorldMetrics.HEIGHT - 140f - index * (cardHeight + cardGap)
        level to Rectangle(WorldMetrics.WIDTH / 2f - cardWidth / 2f, top - cardHeight, cardWidth, cardHeight)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        handleInput()

        Gdx.gl.glClearColor(0.08f, 0.10f, 0.14f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.update()
        val shapes = game.shapes
        shapes.projectionMatrix = camera.combined
        shapes.begin(ShapeRenderer.ShapeType.Filled)
        shapes.color = Color(0.20f, 0.24f, 0.30f, 1f)
        shapes.rect(backButton.x, backButton.y, backButton.width, backButton.height)
        for ((level, rect) in cards) {
            val unlocked = game.saveData.isUnlocked(level.id)
            shapes.color = if (unlocked) Color(0.16f, 0.36f, 0.34f, 1f) else Color(0.18f, 0.18f, 0.20f, 1f)
            shapes.rect(rect.x, rect.y, rect.width, rect.height)
        }
        shapes.end()

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.font.data.setScale(1.1f)
        game.font.color = Color.WHITE
        game.font.draw(game.batch, "Zurueck", backButton.x, backButton.y + backButton.height / 2f + 8f, backButton.width, Align.center, false)

        game.font.data.setScale(1.8f)
        game.font.draw(game.batch, "Levelauswahl", 0f, WorldMetrics.HEIGHT - 40f, WorldMetrics.WIDTH, Align.center, false)

        for ((level, rect) in cards) {
            val unlocked = game.saveData.isUnlocked(level.id)
            game.font.color = if (unlocked) Color.WHITE else Color(0.55f, 0.55f, 0.55f, 1f)
            game.font.data.setScale(1.3f)
            game.font.draw(game.batch, level.name, rect.x + 24f, rect.y + rect.height - 20f)
            game.font.data.setScale(1.0f)
            val subtitle = if (unlocked) level.description else "Gesperrt - schliesse das vorherige Level ab"
            game.font.draw(game.batch, subtitle, rect.x + 24f, rect.y + rect.height - 55f)
        }
        game.batch.end()
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return
        touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(touchPoint)

        if (backButton.contains(touchPoint.x, touchPoint.y)) {
            game.setScreen(MainMenuScreen(game))
            return
        }
        for ((level, rect) in cards) {
            if (rect.contains(touchPoint.x, touchPoint.y) && game.saveData.isUnlocked(level.id)) {
                game.setScreen(GameScreen(game, level))
                return
            }
        }
    }
}
