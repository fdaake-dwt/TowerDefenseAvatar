package com.felixdaake.elementaldefense.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.felixdaake.elementaldefense.ElementalDefenseGame
import com.felixdaake.elementaldefense.WorldMetrics

/** Title screen: game name and a single "Spielen" button leading to the level select. */
class MainMenuScreen(private val game: ElementalDefenseGame) : ScreenAdapter() {

    private val camera = OrthographicCamera()
    private val viewport: Viewport = FitViewport(WorldMetrics.WIDTH, WorldMetrics.HEIGHT, camera)
    private val touchPoint = Vector3()

    private val playButton = Rectangle(
        WorldMetrics.WIDTH / 2f - 110f, WorldMetrics.HEIGHT / 2f - 110f, 220f, 64f
    )

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
        shapes.color = Color(0.16f, 0.45f, 0.30f, 1f)
        shapes.rect(playButton.x, playButton.y, playButton.width, playButton.height)
        shapes.end()

        game.batch.projectionMatrix = camera.combined
        game.batch.begin()
        game.font.color = Color.WHITE
        game.font.data.setScale(2.2f)
        game.font.draw(
            game.batch, "Elemental Defense", 0f, WorldMetrics.HEIGHT - 90f,
            WorldMetrics.WIDTH, com.badlogic.gdx.utils.Align.center, false
        )
        game.font.data.setScale(1.1f)
        game.font.draw(
            game.batch,
            "Die vier Elemente schuetzen das Dorf vor Raeuberbanden",
            0f, WorldMetrics.HEIGHT - 150f,
            WorldMetrics.WIDTH, com.badlogic.gdx.utils.Align.center, false
        )
        game.font.data.setScale(1.3f)
        game.font.draw(
            game.batch, "Spielen",
            playButton.x, playButton.y + playButton.height / 2f + 10f,
            playButton.width, com.badlogic.gdx.utils.Align.center, false
        )
        game.batch.end()
    }

    private fun handleInput() {
        if (!Gdx.input.justTouched()) return
        touchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)
        viewport.unproject(touchPoint)
        if (playButton.contains(touchPoint.x, touchPoint.y)) {
            game.setScreen(LevelSelectScreen(game))
        }
    }
}
