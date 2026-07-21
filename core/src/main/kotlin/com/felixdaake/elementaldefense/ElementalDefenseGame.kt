package com.felixdaake.elementaldefense

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.felixdaake.elementaldefense.logic.SaveData
import com.felixdaake.elementaldefense.screens.MainMenuScreen

/**
 * Root LibGDX application. Owns the few resources shared by every screen
 * (batch, shape renderer, font, save data) and starts on the main menu.
 */
class ElementalDefenseGame : Game() {

    lateinit var batch: SpriteBatch
        private set
    lateinit var shapes: ShapeRenderer
        private set
    lateinit var font: BitmapFont
        private set
    lateinit var saveData: SaveData
        private set

    override fun create() {
        batch = SpriteBatch()
        shapes = ShapeRenderer()
        font = BitmapFont()
        font.data.setScale(1.2f)
        saveData = SaveData()
        setScreen(MainMenuScreen(this))
    }

    override fun dispose() {
        screen?.dispose()
        batch.dispose()
        shapes.dispose()
        font.dispose()
    }
}
