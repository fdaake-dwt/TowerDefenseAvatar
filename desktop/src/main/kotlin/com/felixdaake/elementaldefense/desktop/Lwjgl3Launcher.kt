package com.felixdaake.elementaldefense.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.felixdaake.elementaldefense.ElementalDefenseGame
import com.felixdaake.elementaldefense.WorldMetrics

/** Desktop entry point, used to develop/test the game without an Android device or emulator. */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Elemental Defense")
        setWindowedMode(WorldMetrics.WIDTH.toInt(), WorldMetrics.HEIGHT.toInt())
        useVsync(true)
        setForegroundFPS(60)
    }
    Lwjgl3Application(ElementalDefenseGame(), config)
}
