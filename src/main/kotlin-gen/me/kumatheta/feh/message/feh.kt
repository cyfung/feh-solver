package me.kumatheta.feh.message

import kotlinx.serialization.Serializable

@Serializable
data class UnitAdded(
    val name: String,
    val unitId: Int,
    val maxHp: Int,
    val playerControl: Boolean,
    val startX: Int,
    val startY: Int,
    val moveType: MoveType,
    val attackType: AttackType
)

enum class MoveType {
    INFANTRY,
    ARMORED,
    CAVALRY,
    FLIER
}

enum class AttackType {
    SWORD,
    LANCE,
    AXE,
    RED_BOW,
    BLUE_BOW,
    GREEN_BOW,
    COLORLESS_BOW,
    RED_DAGGER,
    BLUE_DAGGER,
    GREEN_DAGGER,
    COLORLESS_DAGGER,
    RED_TOME,
    BLUE_TOME,
    GREEN_TOME,
    STAFF,
    RED_BREATH,
    BLUE_BREATH,
    GREEN_BREATH,
    COLORLESS_BREATH,
    RED_BEAST,
    BLUE_BEAST,
    GREEN_BEAST,
    COLORLESS_BEAST
}

@Serializable
data class Terrain(
    val type: Type,
    val isDefenseTile: Boolean
) {
    enum class Type {
        REGULAR,
        WALL,
        FLIER_ONLY,
        FOREST,
        TRENCH
    }
}

@Serializable
data class BattleMapPosition(
    val x: Int,
    val y: Int,
    val terrain: Terrain,
    val obstacle: Int
)

@Serializable
data class BattleMap(
    val sizeX: Int,
    val sizeY: Int,
    val battleMapPositions: List<BattleMapPosition> = emptyList()
)

@Serializable
data class SetupInfo(
    val battleMap: BattleMap,
    val unitsAdded: List<UnitAdded>
)

@Serializable
data class Action(
    val unitId: Int,
    val moveX: Int,
    val moveY: Int,
    val targetUnitId: Int?,
    val obstacleX: Int?,
    val obstacleY: Int?
)

@Serializable
data class UnitUpdate(
    val unitId: Int,
    val currentHp: Int?,
    val active: Boolean?,
    val x: Int?,
    val y: Int?
)

@Serializable
data class UpdateInfo(
    val action: Action?,
    val unitUpdates: List<UnitUpdate>,
    val unitsAdded: List<UnitAdded>
)

@Serializable
data class MoveSet(
    val moves: List<UpdateInfo>,
    val score: Long,
    val tries: Int
)