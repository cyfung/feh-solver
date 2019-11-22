package me.kumatheta.feh.test

import me.kumatheta.feh.*
import me.kumatheta.feh.mcts.FehBoard
import me.kumatheta.feh.skill.assist.Pivot
import me.kumatheta.feh.skill.assist.Smite
import me.kumatheta.feh.skill.weapon.*
import me.kumatheta.mcts.Mcts
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.time.ExperimentalTime

object Alfonse : HeroModel(
    "Alfonse",
    null,
    null,
    MoveType.INFANTRY,
    Folkvangr,
    null,
    null,
    Stat(24, 31, 10, 13, 9),
    emptyList(),
    true
)

object Sharena : HeroModel(
    "Sharena",
    null,
    null,
    MoveType.INFANTRY,
    Fensalir,
    null,
    null,
    Stat(24, 29, 13, 12, 9),
    emptyList(),
    true
)

object Anna :
    HeroModel("Anna", null, null, MoveType.INFANTRY, Noatun, null, null, Stat(24, 28, 16, 9, 11), emptyList(), true)

object AxeFighter : HeroModel(
    "AxeFighter",
    null,
    null,
    MoveType.INFANTRY,
    SlayingHammerPlus,
    null,
    null,
    Stat(25, 26, 12, 13, 9),
    emptyList(),
    true
)

object LanceFighter : HeroModel(
    "LanceFighter",
    null,
    null,
    MoveType.INFANTRY,
    IronLance,
    Pivot,
    null,
    Stat(23, 13, 12, 8, 6),
    emptyList(),
    true
)

object SwordFighter : HeroModel(
    "SwordFighter",
    null,
    null,
    MoveType.INFANTRY,
    IronSword,
    null,
    null,
    Stat(25, 25, 12, 13, 9),
    emptyList(),
    true
)

object Fir :
    HeroModel("Fir", null, null, MoveType.INFANTRY, Folkvangr, null, null, Stat(27, 36, 19, 9, 12), emptyList(), true)

object Bartre :
    HeroModel("Bartre", null, null, MoveType.INFANTRY, IronAxe, Smite, null, Stat(27, 31, 13, 16, 8), emptyList(), true)

object Effie :
    HeroModel("Effie", null, null, MoveType.ARMORED, IronLance, Smite, null, Stat(28, 33, 9, 16, 8), emptyList(), true)

object TestMap : BattleMap {
    private val flierTerrain = listOf(
        0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5,
        1 to 2, 1 to 5, 2 to 2, 3 to 0, 3 to 1, 3 to 2, 3 to 3, 3 to 4, 3 to 5,
        4 to 0, 4 to 1, 4 to 2, 4 to 5, 5 to 5
    ).asSequence().map {
        Position(it.second, it.first)
    }.toSet()

    override fun getTerrain(position: Position): Terrain {
        return if (flierTerrain.contains(position)) {
            Terrain.FLIER_ONLY
        } else {
            Terrain.REGULAR
        }
    }

    override val size: Position = Position(6, 6)
    val map = mapOf(
        Position(0, 2) to HeroUnit(1, Bartre, Team.PLAYER, Position(0, 2)),
        Position(1, 2) to HeroUnit(2, Fir, Team.PLAYER, Position(1, 2)),
        Position(4, 1) to HeroUnit(3, Effie, Team.PLAYER, Position(4, 1)),
        Position(0, 5) to HeroUnit(4, SwordFighter, Team.ENEMY, Position(0, 5)),
        Position(5, 2) to HeroUnit(5, AxeFighter, Team.ENEMY, Position(5, 2))
    )

    init {
        map.forEach { (position, chessPiece) ->
            if (chessPiece !is HeroUnit) return@forEach
            chessPiece.position = position
        }
    }

    override fun toChessPieceMap(): Map<Position, ChessPiece> {
        return map
    }
}

@ExperimentalTime
fun main() {
//    val solver = BattleSolver(TestMap, 10)
//    solver.solve()

    val positionMap = readMap(Paths.get("test/feh - map.csv"))
    val (_, spawnMap) = readUnits(Paths.get("test/feh - spawn.csv"))
    val playerMap = spawnMap.asSequence().map {
        (it.key - 4) to it.value.heroModel
    }.toMap()

    val state =
        BattleState(BasicBattleMap(positionMap, spawnMap, playerMap))//mapOf(1 to Effie, 2 to Bartre, 3 to Fir)))
    val phraseLimit = 7
    val board = FehBoard(phraseLimit, state)
    val mcts = Mcts(board)
    repeat(10) {
        mcts.run(1000)
        val bestMoves = mcts.getBestMoves()
        println("best score: ${mcts.getBestScore()}")
        val testState = board.tryMoves(bestMoves)
        bestMoves.forEach {
            println(it)
        }
        println("${testState.enemyDied}, ${testState.playerDied}")

    }
}


