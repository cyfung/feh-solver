package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    private val state: BattleState,
    override val score: Long?,
    private val totalPlayerHp: Int
) : Board<FehMove> {
    private val enemyCount: Int = state.enemyCount
    private val playerCount: Int = state.playerCount
    override val moves: List<FehMove> by lazy {
        if (score != null) {
            emptyList()
        } else {
            state.getAllPlayerMovements().map {
                FehMove(it)
            }.toList()
        }
    }

    constructor(phraseLimit: Int, state: BattleState) : this(
        phraseLimit,
        state,
        null,
        state.unitsSeq(Team.PLAYER).sumBy { it.stat.hp }
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
            if (state.playerDied > 0 || state.winningTeam != null || phraseLimit < state.phrase) {
                calculateScore(state)
            } else if (!state.engaged && state.phrase > 6) {
                0L
            } else {
                null
            }
        } else {
            null
        }
        return FehBoard(phraseLimit, state, score, totalPlayerHp)
    }

    fun calculateScore(battleState: BattleState) =
        battleState.enemyDied * 500L + (battleState.playerCount - battleState.playerDied) * 500L +
                battleState.unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 + +battleState.unitsSeq(
            Team.ENEMY
        ).sumBy { it.stat.hp - it.currentHp } * 2 + (phraseLimit - battleState.phrase) * 20

    private val stateCopy
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

    override fun suggestedMoves(nextMoves: List<FehMove>): Sequence<FehMove> {
        val enemyCount = enemyCount
        return nextMoves.asSequence().filter { it.unitAction is MoveAndAttack }.filter {
            applyMove(it).enemyCount < enemyCount
        }
    }
}

data class FehMove(val unitAction: UnitAction) : Move