package com.felixdaake.elementaldefense.logic

import com.badlogic.gdx.Gdx

/** Persists which levels the player has unlocked so far, across app restarts. */
class SaveData {

    private val prefs = Gdx.app.getPreferences("elemental-defense-save")

    /** Highest level id (0-based) the player is allowed to play right now. */
    fun highestUnlockedLevel(): Int = prefs.getInteger(KEY_UNLOCKED, 0)

    fun isUnlocked(levelId: Int): Boolean = levelId <= highestUnlockedLevel()

    fun unlockUpTo(levelId: Int) {
        if (levelId > highestUnlockedLevel()) {
            prefs.putInteger(KEY_UNLOCKED, levelId)
            prefs.flush()
        }
    }

    companion object {
        private const val KEY_UNLOCKED = "highestUnlockedLevel"
    }
}
