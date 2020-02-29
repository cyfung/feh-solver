package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveAndAssist
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    private val state: BattleState,
    override val score: Long?,
    private val totalPlayerHp: Int,
    private val maxTurnBeforeEngage: Int
) : Board<FehMove> {
    override val moves: List<FehMove> by lazy {
        if (score != null) {
            emptyList()
        } else {
            state.getAllPlayerMovements().map {
                FehMove(it)
            }.toList()
        }
    }

    constructor(phaseLimit: Int, state: BattleState, maxTurnBeforeEngage: Int) : this(
        phaseLimit,
        state,
        null,
        state.unitsSeq(Team.PLAYER).sumBy { it.maxHp },
        maxTurnBeforeEngage
    )


    override fun applyMove(move: FehMove): FehBoard {
        check(score == null)
        val state = state.copy()
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        val score: Long? = if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
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
        return FehBoard(phraseLimit, state, score, totalPlayerHp, maxTurnBeforeEngage)
    }

    fun calculateScore(battleState: BattleState) =
        battleState.enemyDied * 500L + (battleState.playerCount - battleState.playerDied) * 500L +
            battleState.unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 + +battleState.unitsSeq(
            Team.ENEMY
        ).sumBy { it.maxHp - it.currentHp } * 2 + (phraseLimit - battleState.phase) * 20 + if(battleState.phase >= phraseLimit) {
            -1500L
        } else {
            0L
        }

    val stateCopy
        get() = state.copy()

    fun tryMoves(moves: List<FehMove>, printMoves: Boolean = false): BattleState {
        val testState = stateCopy
        moves.forEach {
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
        return nextMoves.asSequence().sortedBy {
            if (it.unitAction is MoveAndAttack) {
                1
            } else if (it.unitAction is MoveAndAssist) {
                val assist = state.getUnit(it.unitAction.heroUnitId).assist
                if (assist is Refresh || assist is Heal) {
                    0
                } else {
                    2
                }
            } else {
                2
            }
        }
    }

    fun tryAndGetDetails(moves: List<FehMove>): List<Pair<UnitAction?, BattleState>> {
        val currentState = stateCopy
        return moves.asSequence().flatMap {
            val unitAction = it.unitAction
            val movementResult = currentState.playerMove(unitAction)
            val seq = sequenceOf(Pair<UnitAction?, BattleState>(unitAction, currentState.copy()))
            when {
                movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER -> {
                    seq
                }
                movementResult.phraseChange -> {
                    seq + currentState.enemyMoves { enemyAction ->
                        Pair<UnitAction?, BattleState>(enemyAction, currentState.copy())
                    }.asSequence() + Pair<UnitAction?, BattleState>(null, currentState.copy())
                }
                else -> seq
            }
        }.toList()
    }
}

data class FehMove(val unitAction: UnitAction) : Move