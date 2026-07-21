package com.felixdaake.elementaldefense.android

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.felixdaake.elementaldefense.ElementalDefenseGame

/** Android entry point: hosts the LibGDX app in a full-screen GLSurfaceView. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
        }
        initialize(ElementalDefenseGame(), config)
    }
}
