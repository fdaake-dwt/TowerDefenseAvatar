package com.felixdaake.elementaldefense.model

import com.badlogic.gdx.math.Vector2

/** Static registry of all tracks in the game, in unlock order. */
object Levels {

    val all: List<Level> = listOf(riverVillage, mountainPass)

    fun byId(id: Int): Level = all.first { it.id == id }

    private val riverVillage: Level
        get() = Level(
            id = 0,
            name = "Dorf am Fluss",
            description = "Räuber ziehen den Fluss entlang zum Marktplatz.",
            path = listOf(
                Vector2(-40f, 380f),
                Vector2(160f, 380f),
                Vector2(160f, 200f),
                Vector2(420f, 200f),
                Vector2(420f, 430f),
                Vector2(700f, 430f),
                Vector2(700f, 140f),
                Vector2(1000f, 140f)
            ),
            startingCurrency = 150,
            startingLives = 20,
            waves = listOf(
                Wave(listOf(SpawnEntry(EnemyType.GRUNT_RAIDER, 6, 0.9f))),
                Wave(listOf(SpawnEntry(EnemyType.GRUNT_RAIDER, 8, 0.8f))),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.GRUNT_RAIDER, 6, 0.8f),
                        SpawnEntry(EnemyType.SCOUT_RAIDER, 4, 0.5f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.ICY_RAIDER, 6, 1.0f),
                        SpawnEntry(EnemyType.GRUNT_RAIDER, 6, 0.7f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.PLANT_RAIDER, 5, 1.1f),
                        SpawnEntry(EnemyType.SCOUT_RAIDER, 6, 0.5f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.ICY_RAIDER, 8, 0.8f, hpMultiplier = 1.2f),
                        SpawnEntry(EnemyType.PLANT_RAIDER, 6, 0.9f, hpMultiplier = 1.2f),
                        SpawnEntry(EnemyType.GRUNT_RAIDER, 10, 0.5f)
                    ),
                    delayAfter = 0f
                )
            )
        )

    private val mountainPass: Level
        get() = Level(
            id = 1,
            name = "Verschneites Bergdorf",
            description = "Eisige Räuberbanden stürmen den Bergpass hinab.",
            path = listOf(
                Vector2(480f, -40f),
                Vector2(480f, 120f),
                Vector2(200f, 120f),
                Vector2(200f, 300f),
                Vector2(560f, 300f),
                Vector2(560f, 460f),
                Vector2(860f, 460f),
                Vector2(860f, 580f)
            ),
            startingCurrency = 170,
            startingLives = 18,
            waves = listOf(
                Wave(listOf(SpawnEntry(EnemyType.ICY_RAIDER, 8, 0.8f))),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.ICY_RAIDER, 6, 0.7f),
                        SpawnEntry(EnemyType.SCOUT_RAIDER, 6, 0.4f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.PLANT_RAIDER, 7, 0.9f),
                        SpawnEntry(EnemyType.ICY_RAIDER, 6, 0.8f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.GRUNT_RAIDER, 10, 0.5f),
                        SpawnEntry(EnemyType.SCOUT_RAIDER, 8, 0.4f)
                    )
                ),
                Wave(
                    listOf(
                        SpawnEntry(EnemyType.ICY_RAIDER, 10, 0.7f, hpMultiplier = 1.3f),
                        SpawnEntry(EnemyType.PLANT_RAIDER, 8, 0.8f, hpMultiplier = 1.3f),
                        SpawnEntry(EnemyType.SCOUT_RAIDER, 10, 0.4f)
                    ),
                    delayAfter = 0f
                )
            )
        )
}
