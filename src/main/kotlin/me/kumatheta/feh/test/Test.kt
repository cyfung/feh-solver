package me.kumatheta.feh.test

import me.kumatheta.feh.*
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.skill.weapon.*
import me.kumatheta.mcts.Mcts

object Alfonse : HeroModel(MoveType.INFANTRY, Folkvangr, null, null, Stat(24, 31, 10, 13, 9), emptyList(), true)
object Sharena : HeroModel(MoveType.INFANTRY, Fensalir, null, null, Stat(24, 29, 13, 12, 9), emptyList(), true)
object Anna : HeroModel(MoveType.INFANTRY, Noatun, null, null, Stat(24, 28, 16, 9, 11), emptyList(), true)

object AxeFighter : HeroModel(MoveType.INFANTRY, IronAxe, null, null, Stat(23, 17, 12, 8, 6), emptyList(), true)
object LanceFighter : HeroModel(MoveType.INFANTRY, IronLance, null, null, Stat(23, 13, 12, 8, 6), emptyList(), true)
object SwordFighter : HeroModel(MoveType.INFANTRY, IronSword, null, null, Stat(23, 17, 12, 8, 6), emptyList(), true)

object TestMap : me.kumatheta.feh.BattleMap {
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

    init {
        map.forEach {(position, heroUnit) ->
            heroUnit.position = position
        }
    }
    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return map
    }
}

fun main() {
//    val solver = BattleSolver(TestMap, 10)
//    solver.solve()

    val state = BattleState(TestMap)
    val phraseLimit = 3
    val board = FehBoard(phraseLimit, state)
    val mcts = Mcts(board)
    repeat(10) {
        mcts.run(1000)
        val bestMoves = mcts.getBestMoves()
        println(bestMoves)
        val testState = board.stateCopy
        bestMoves.forEach {
            val unitAction = it.unitAction
            println(unitAction)
            val movementResult = testState.playerMove(unitAction)
            if (movementResult.phraseChange) {
                testState.enemyMoves().forEach(::println)
            }
        }
        println("${testState.enemyDied}, ${testState.playerDied}")

    }
}


