package me.kumatheta.feh.mcts

import com.marcinmoskala.math.permutations
import me.kumatheta.feh.*
import me.kumatheta.feh.skill.MovementAssist
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move
import kotlin.random.Random

interface FehBoard : Board<FehMove> {
    fun getStateCopy(): BattleState

    override val score: Long?
        get() = null

    override fun applyMove(move: FehMove): FehBoard

    override fun getPlayOutMove(random: Random): FehMove {
        return moves.random(random)
    }
}

abstract class BasicFehBoard(
    private val parent: BasicFehBoard?,
    private val move: FehMove?,
    private val state: BattleState,
    val config: FehBoardConfig
) : FehBoard {
    final override fun getStateCopy(): BattleState {
        return state.copy()
    }

    fun calculateScore(endState: BattleState, move: FehMove): Long {
        return config.calculateScore(endState, config.phaseLimit)
    }

}

fun newFehBoard(
    phaseLimit: Int,
    state: BattleState,
    maxTurnBeforeEngage: Int,
    canRearrange: Boolean = true,
    bossId: Int = 5,
    calculateScore: BattleState.(phaseLimit: Int) -> Long
): FehBoard {
    val internalState = state.copy()
    val totalPlayerHp = internalState.unitsSeq(Team.PLAYER).sumBy { it.maxHp }
    val assistMap = internalState.unitsSeq(Team.PLAYER).associate {
        it.id to it.assist
    }
    val config = FehBoardConfig(
        phaseLimit = phaseLimit,
        totalPlayerHp = totalPlayerHp,
        maxTurnBeforeEngage = maxTurnBeforeEngage,
        assistMap = assistMap,
        bossId = bossId,
        calculateScore = calculateScore
    )

//    val testMoves = listOf(
//        Rearrange(listOf(1, 4, 2, 3)),
//        NormalMove(MoveAndBreak(heroUnitId = 3, moveTargetX = 4, moveTargetY = 2, obstacleX = 3, obstacleY = 1)),
//        NormalMove(MoveAndBreak(heroUnitId = 2, moveTargetX = 2, moveTargetY = 1, obstacleX = 3, obstacleY = 1)),
//        NormalMove(MoveAndAssist(heroUnitId = 4, moveTargetX = 4, moveTargetY = 1, assistTargetId = 3)),
//        NormalMove(MoveAndAttack(heroUnitId = 3, moveTargetX = 4, moveTargetY = 4, attackTargetId = 8)),
//        NormalMove(MoveOnly(heroUnitId = 1, moveTargetX = 1, moveTargetY = 2))//,
//    )
//    testMoves.take(0).forEach { move ->
//        val exists = board.moves.any {
//            it == move
//        }
//        if (!exists) {
//            throw IllegalStateException("moves not exists $move")
//        }
//        board = board.applyMove(move)
//    }

    return if (canRearrange) {
        RearrangeFehBoard(config, internalState)
    } else {
        internalState.rearrange((1..internalState.playerCount).toList())
        newInternalBoardChain(null, null, config, internalState)
    }

}

private fun newInternalBoardChain(
    parent: BasicFehBoard?,
    move: FehMove?,
    config: FehBoardConfig,
    state: BattleState
): FehBoard {
    return StandardFehBoard(parent, move, config, state)
}

class RearrangeFehBoard(
    config: FehBoardConfig,
    private val state: BattleState
) : BasicFehBoard(null, null, state, config) {
    override val moves: List<FehMove> by lazy {
        (1..state.playerCount).toList().permutations().map {
            Rearrange(it)
        }
    }

    override fun applyMove(move: FehMove): FehBoard {
        move as Rearrange
        val stateCopy = getStateCopy()
        stateCopy.rearrange(move.order)
        return newInternalBoardChain(this, move, config, stateCopy)
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        return nextMoves.asSequence()
    }
}

class ScoreFehBoard(
    override val score: Long
) : FehBoard {
    override val moves: List<FehMove>
        get() = emptyList()

    override fun getStateCopy(): BattleState {
        throw UnsupportedOperationException()
    }

    override fun applyMove(move: FehMove): FehBoard {
        throw UnsupportedOperationException()
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        throw UnsupportedOperationException()
    }
}

fun FehBoard.tryMoves(moves: List<FehMove>, printMoves: Boolean = false): BattleState {
    val currentState = getStateCopy()

    moves.forEach {
        when (it) {
            is Rearrange -> {
                currentState.rearrange((moves[0] as Rearrange).order)
            }
            is NormalMove -> {
                val unitAction = it.unitAction
                if (printMoves) {
                    println(unitAction)
                }
                val movementResult = currentState.playerMove(unitAction)
                when {
                    movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER -> {
                        // ignore
                    }
                    movementResult.phraseChange -> {
                        val enemyMoves = currentState.enemyMoves()
                        if (printMoves) {
                            enemyMoves.forEach(::println)
                        }
                    }
                }
            }
            else -> Unit
        }

    }
    return currentState
}

fun FehBoard.tryAndGetDetails(moves: List<FehMove>): List<Pair<UnitAction?, BattleState>> {
    val currentState = getStateCopy()
    return moves.asSequence().mapNotNull {
        when (it) {
            is Rearrange -> {
                currentState.rearrange((moves[0] as Rearrange).order)
                sequenceOf(null to currentState.copy())
            }
            is NormalMove -> {
                val unitAction = it.unitAction
                val movementResult = currentState.playerMove(unitAction)
                val seq = sequenceOf(unitAction to currentState.copy())
                when {
                    movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER -> {
                        seq
                    }
                    movementResult.phraseChange -> {
                        seq + currentState.enemyMoves { enemyAction ->
                            enemyAction to currentState.copy()
                        }.asSequence() + Pair<UnitAction?, BattleState>(null, currentState.copy())
                    }
                    else -> seq
                }
            }
            else -> null
        }
    }.flatMap { it }.toList()
}

class StandardFehBoard(
    parent: BasicFehBoard?,
    move: FehMove?,
    config: FehBoardConfig,
    private val state: BattleState,
    private val playerMoves: Sequence<UnitAction> = state.getAllPlayerMovements()
) : BasicFehBoard(parent, move, state, config) {
    override val moves: List<FehMove> by lazy {
        playerMoves.map {
            NormalMove(it)
        }.toList()
    }

    override fun applyMove(move: FehMove): FehBoard {
        move as NormalMove
        check(score == null)
        val state = getStateCopy()
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        val score = if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
            calculateScore(state, move)
        } else if (movementResult.phraseChange) {
            state.enemyMoves()
            if (state.playerDied > 0 || state.winningTeam != null || config.phaseLimit < state.phase) {
                calculateScore(state, move)
            } else if (!state.engaged && state.phase > config.maxTurnBeforeEngage * 2) {
                0L
            } else {
                null
            }
        } else {
            null
        }

        return if (score == null) {
            newInternalBoardChain(this, move, config, state)
        } else {
            ScoreFehBoard(score)
        }

    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        @Suppress("UNCHECKED_CAST")
        nextMoves as List<NormalMove>
        return nextMoves.asSequence().sortedByDescending { move ->
            val unitAction = move.unitAction
            unitAction.toRating()
        }
    }

    private fun UnitAction.toRating(): Int {
        val assist = config.assistMap[heroUnitId]
        return if (assist is Refresh) {
            if (this is MoveAndAssist) {
                5
            } else {
                0
            }
        } else {
            when (this) {
                is MoveAndAttack -> 3
                is MoveAndAssist -> when (assist) {
                    is Heal -> 3
                    is MovementAssist -> 2
                    else -> 1
                }
                else -> 1
            }
        }
//        val attackers = enemiesInRange(this)
//        attackers.mapNotNull {
//            config.trialResults[AttackerDefenderPair(it, playerUnit)]
//        }.forEach {
//            if (it.defenderHpAfter == 0) {
//                score-=10
//                return@forEach
//            }
//            if (it.attackerHpAfter == 0) {
//                score+=2
//            }
//            if (it.defenderHpAfter * 2 < it.defenderHpBefore) {
//                score-=2
//            }
//        }
    }

//    private fun enemiesInRange(unitAction: UnitAction): Sequence<Int> {
//        val enemies = dangerAreas[unitAction.moveTarget] ?: return emptySequence()
//        val attackTargetId = (unitAction as? MoveAndAttack)?.attackTargetId ?: return enemies.asSequence()
//        return enemies.asSequence() - attackTargetId
//    }

    override fun getPlayOutMove(random: Random): FehMove {
        return suggestedOrder(moves.shuffled(random)).first()
    }

}

interface FehMove : Move
data class NormalMove(val unitAction: UnitAction) : FehMove {
    override fun toString(): String {
        return "NormalMove($unitAction)"
    }
}

data class Rearrange(val order: List<Int>) : FehMove {
    override fun toString(): String {
        return "Rearrange(listOf(${order.joinToString()}))"
    }
}

enum class FehActionType {
    REFRESH,
    ATTACK,
    HEAL,
    OTHERS
}

data class GroupMove<T>(val value: T) : FehMove