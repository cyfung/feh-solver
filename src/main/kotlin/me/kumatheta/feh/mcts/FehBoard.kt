package me.kumatheta.feh.mcts

import com.marcinmoskala.math.permutations
import me.kumatheta.feh.*
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    private val state: BattleState,
    override val score: Long?,
    private val totalPlayerHp: Int,
    private val maxTurnBeforeEngage: Int,
    private val canRearrange: Boolean
) : Board<FehMove> {

    override val moves: List<FehMove> by lazy {
        when {
            canRearrange -> listOf(1, 2, 3, 4).permutations().map {
                Rearrange(it)
            }
            score != null -> emptyList()
            else -> state.getAllPlayerMovements().map {
                NormalMove(it)
            }.toList()
        }
    }

    constructor(phaseLimit: Int, state: BattleState, maxTurnBeforeEngage: Int, canRearrange: Boolean = true) : this(
        phaseLimit,
        state,
        null,
        state.unitsSeq(Team.PLAYER).sumBy { it.maxHp },
        maxTurnBeforeEngage,
        canRearrange
    )


    override fun applyMove(move: FehMove): FehBoard {
        check(score == null)
        val state = state.copy()
        val score = when (canRearrange) {
            true -> {
                move as Rearrange
                state.rearrange(move.order)
                null
            }
            false -> {
                move as NormalMove
                val nextMove = move.unitAction
                val movementResult = state.playerMove(nextMove)
                if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
                    calculateScore(state)
                } else if (movementResult.phraseChange) {
                    state.enemyMoves()
                    if (state.playerDied > 0 || state.winningTeam != null || phraseLimit < state.phase) {
                        calculateScore(state)
                    } else if (!state.engaged && state.phase > maxTurnBeforeEngage * 2) {
                        0L
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }
        return FehBoard(
            phraseLimit = phraseLimit,
            state = state,
            score = score,
            totalPlayerHp = totalPlayerHp,
            maxTurnBeforeEngage = maxTurnBeforeEngage,
            canRearrange = false
        )
    }

    fun calculateScore(battleState: BattleState) =
        battleState.enemyDied * 500L + (battleState.playerCount - battleState.playerDied) * 500L +
                battleState.unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 + +battleState.unitsSeq(
            Team.ENEMY
        ).sumBy { it.maxHp - it.currentHp } * 2 + (phraseLimit - battleState.phase) * 20 + if (battleState.phase >= phraseLimit) {
            -1500L
        } else {
            0L
        }

    val stateCopy
        get() = state.copy()

    fun tryMoves(moves: List<FehMove>, printMoves: Boolean = false): BattleState {
        val testState = stateCopy
        @Suppress("UNCHECKED_CAST") val normalMoves = (if (canRearrange) {
            testState.rearrange((moves[0] as Rearrange).order)
            moves.subList(1, moves.size)
        } else {
            moves
        }) as List<NormalMove>
        normalMoves.forEach {
            val unitAction = it.unitAction
            if (printMoves) {
                println(unitAction)
            }
            val movementResult = testState.playerMove(unitAction)
            when {
                movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER -> {
                    // ignore
                }
                movementResult.phraseChange -> {
                    val enemyMoves = testState.enemyMoves()
                    if (printMoves) {
                        enemyMoves.forEach(::println)
                    }
                }
            }
        }
        return testState
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        if (canRearrange) {
           return nextMoves.asSequence()
        }
        @Suppress("UNCHECKED_CAST")
        return (nextMoves as List<NormalMove>).asSequence().sortedBy {
            when (it.unitAction) {
                is MoveAndAttack -> 1
                is MoveAndAssist -> when (state.getUnit(it.unitAction.heroUnitId).assist) {
                    is Refresh -> 0
                    is Heal -> 1
                    else -> 2
                }
                else -> 2
            }
        }
    }

    fun tryAndGetDetails(moves: List<FehMove>): List<Pair<UnitAction?, BattleState>> {
        val currentState = stateCopy
        val startingSeq : Sequence<Pair<UnitAction?, BattleState>>
        @Suppress("UNCHECKED_CAST")
        val normalMoves = (if (canRearrange) {
            currentState.rearrange((moves[0] as Rearrange).order)
            startingSeq = sequenceOf(null to currentState.copy())
            moves.subList(1, moves.size)
        } else {
            startingSeq = emptySequence()
            moves
        }) as List<NormalMove>
        return (startingSeq + normalMoves.asSequence().flatMap {
            val unitAction = it.unitAction
            val movementResult = currentState.playerMove(unitAction)
            val seq = sequenceOf(Pair<UnitAction?, BattleState>(unitAction, currentState.copy()))
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
        }).toList()
    }
}

sealed class FehMove : Move
data class NormalMove(val unitAction: UnitAction) : FehMove() {
    override fun toString(): String {
        return "NormalMove(${unitAction.toString()})"
    }
}
data class Rearrange(val order: List<Int>) : FehMove() {
    override fun toString(): String {
        return "Rearrange(listOf(${order.joinToString()}))"
    }
}
