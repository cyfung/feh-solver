package me.kumatheta.ws

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import me.kumatheta.feh.Axe
import me.kumatheta.feh.BasicBattleMap
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Beast
import me.kumatheta.feh.Bow
import me.kumatheta.feh.Color
import me.kumatheta.feh.Dagger
import me.kumatheta.feh.Dragon
import me.kumatheta.feh.Lance
import me.kumatheta.feh.Magic
import me.kumatheta.feh.MoveType
import me.kumatheta.feh.PositionMap
import me.kumatheta.feh.Staff
import me.kumatheta.feh.Sword
import me.kumatheta.feh.Team
import me.kumatheta.feh.Terrain
import me.kumatheta.feh.Weapon
import me.kumatheta.feh.WeaponType
import me.kumatheta.feh.message.AttackType
import me.kumatheta.feh.message.BattleMapPosition
import me.kumatheta.feh.message.SetupInfo
import me.kumatheta.feh.message.UnitAdded
import me.kumatheta.feh.readMap
import me.kumatheta.feh.readUnits
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.nio.file.Paths

typealias MsgTerrain = me.kumatheta.feh.message.Terrain
typealias MsgBattleMap = me.kumatheta.feh.message.BattleMap
typealias MsgMoveType = me.kumatheta.feh.message.MoveType

private fun Terrain.toMsgTerrain(): MsgTerrain {
    val msgType = when (type) {
        Terrain.Type.WALL -> me.kumatheta.feh.message.Terrain.Type.WALL
        Terrain.Type.FLIER_ONLY -> me.kumatheta.feh.message.Terrain.Type.FLIER_ONLY
        Terrain.Type.FOREST -> me.kumatheta.feh.message.Terrain.Type.FOREST
        Terrain.Type.TRENCH -> me.kumatheta.feh.message.Terrain.Type.TRENCH
        Terrain.Type.REGULAR -> me.kumatheta.feh.message.Terrain.Type.REGULAR
    }
    return MsgTerrain(type = msgType, isDefenseTile = isDefenseTile)
}

private fun MoveType.toMsgMoveType(): MsgMoveType {
    return when (this) {
        MoveType.INFANTRY -> MsgMoveType.INFANTRY
        MoveType.ARMORED -> MsgMoveType.ARMORED
        MoveType.CAVALRY -> MsgMoveType.CAVALRY
        MoveType.FLYING -> MsgMoveType.FLIER
    }
}

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val positionMap = readMap(Paths.get("test/feh - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("test/feh - spawn.csv"))
    val (playerMap, _) = readUnits(Paths.get("test/feh - players.csv"))
    val battleMap = BasicBattleMap(
        positionMap,
        spawnMap,
        playerMap
    )
    val state = BattleState(
        battleMap
    )
    val setupInfo = buildSetupInfo(positionMap, battleMap, state)
    val json = Json(JsonConfiguration.Stable)

    routing {
        get("/") {
            call.respondText(json.stringify(SetupInfo.serializer(), setupInfo))
        }
    }
}

private fun buildSetupInfo(
    positionMap: PositionMap,
    battleMap: BasicBattleMap,
    state: BattleState
): SetupInfo {
    val battleMapPositions = positionMap.terrainMap.map { (position, terrain) ->
        BattleMapPosition(
            x = position.x,
            y = position.y,
            terrain = terrain.toMsgTerrain(),
            obstacle = positionMap.obstacles[position] ?: 0
        )
    }
    val msgBattleMap = MsgBattleMap(
        sizeX = battleMap.size.x,
        sizeY = battleMap.size.y,
        battleMapPositions = battleMapPositions
    )
    val unitsAdded = (state.unitsSeq(Team.PLAYER) + state.unitsSeq(Team.ENEMY)).map {
        UnitAdded(
            name = it.name,
            unitId = it.id,
            maxHp = it.stat.hp,
            playerControl = it.team == Team.PLAYER,
            startX = it.position.x,
            startY = it.position.y,
            moveType = it.moveType.toMsgMoveType(),
            attackType = it.weaponType.toAttackType()
        )
    }.toList()
    return SetupInfo(msgBattleMap, unitsAdded)
}

private fun WeaponType.toAttackType(): AttackType {
    return when (this) {
        is Dagger -> when (color) {
            Color.RED -> AttackType.RED_DAGGER
            Color.GREEN -> AttackType.GREEN_DAGGER
            Color.BLUE -> AttackType.BLUE_DAGGER
            Color.COLORLESS -> AttackType.COLORLESS_DAGGER
        }
        is Bow -> when (color) {
            Color.RED -> AttackType.RED_BOW
            Color.GREEN -> AttackType.GREEN_BOW
            Color.BLUE -> AttackType.BLUE_BOW
            Color.COLORLESS -> AttackType.COLORLESS_BOW
        }
        is Beast-> when (color) {
            Color.RED -> AttackType.RED_BEAST
            Color.GREEN -> AttackType.GREEN_BEAST
            Color.BLUE -> AttackType.BLUE_BEAST
            Color.COLORLESS -> AttackType.COLORLESS_BEAST
        }
        is Dragon -> when (color) {
            Color.RED -> AttackType.RED_BREATH
            Color.GREEN -> AttackType.GREEN_BREATH
            Color.BLUE -> AttackType.BLUE_BREATH
            Color.COLORLESS -> AttackType.COLORLESS_BREATH
        }
        Staff -> AttackType.STAFF
        Sword -> AttackType.SWORD
        Lance -> AttackType.LANCE
        Axe -> AttackType.AXE
        is Magic ->  when (color) {
            Color.RED -> AttackType.RED_TOME
            Color.GREEN -> AttackType.GREEN_TOME
            Color.BLUE -> AttackType.BLUE_TOME
            Color.COLORLESS -> throw IllegalArgumentException("no colorless tome")
        }
    }
}

