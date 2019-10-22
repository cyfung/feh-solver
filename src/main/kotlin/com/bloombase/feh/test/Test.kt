package com.bloombase.feh.test

import com.bloombase.feh.*
import com.bloombase.feh.skill.weapon.*

object Alfonse : HeroModel(MoveType.INFANTRY, Folkvangr, null, null, Stat(24, 31, 10, 13, 9), emptyList(), true)
object Sharena : HeroModel(MoveType.INFANTRY, Fensalir, null, null, Stat(24, 29, 13, 12, 9), emptyList(), true)
object Anna : HeroModel(MoveType.INFANTRY, Noatun, null, null, Stat(24, 28, 16, 9, 11), emptyList(), true)

object AxeFighter : HeroModel(MoveType.INFANTRY, IronAxe, null, null, Stat(23, 17, 12, 8, 6), emptyList(), true)
object LanceFighter : HeroModel(MoveType.INFANTRY, IronLance, null, null, Stat(23, 13, 12, 8, 6), emptyList(), true)
object SwordFighter : HeroModel(MoveType.INFANTRY, IronSword, null, null, Stat(23, 17, 12, 8, 6), emptyList(), true)

object TestMap : BattleMap {
    override fun getTerrain(position: Position): Terrain {
        return Terrain.REGULAR
    }

    override val size: Position = Position(8,8)
    val map = mapOf(
        Position(1, 2) to HeroUnit(1, Alfonse, Team.PLAYER),
        Position(1, 3) to HeroUnit(2, Sharena, Team.PLAYER),
        Position(1,4) to HeroUnit(3, Anna, Team.PLAYER),
        Position(3, 2) to HeroUnit(4, AxeFighter, Team.ENEMY),
        Position(3, 3) to HeroUnit(5, LanceFighter, Team.ENEMY),
        Position(3,4) to HeroUnit(6, SwordFighter, Team.ENEMY)
    )
    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return map
    }
}

fun main() {
    BattleSolver(TestMap).solve()
}