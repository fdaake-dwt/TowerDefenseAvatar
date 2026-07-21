package com.felixdaake.elementaldefense.model

import com.badlogic.gdx.graphics.Color

/**
 * The four bending elements, used both as tower categories and as
 * resistance/weakness tags on enemies.
 */
enum class Element(val displayName: String, val color: Color) {
    FEUER("Feuer", Color(0.90f, 0.35f, 0.15f, 1f)),
    WASSER("Wasser", Color(0.20f, 0.55f, 0.95f, 1f)),
    ERDE("Erde", Color(0.55f, 0.40f, 0.20f, 1f)),
    LUFT("Luft", Color(0.85f, 0.90f, 0.95f, 1f))
}
