package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    state: BattleState,
    score: Double?
) : Board<FehMove> {
    private val state = state.copy()
    private val enemyCount: Int
            get() = state.enemyCount
    private val playerCount: Int
        get() = state.playerCount

    constructor(phraseLimit: Int, state: BattleState) :
            this(
                phraseLimit,
                state,
                null
            )

    override val moves: List<FehMove>
        get() {
            if (score != null) {
                return emptyList()
            }
            return state.getAllPlayerMovements().map {
                FehMove(it)
            }.toList()
        }

    override var score: Double? = score
        private set

    override fun copy(): Board<FehMove> {
        return FehBoard(phraseLimit, state, score)
    }

    override fun applyMove(move: FehMove) {
        check(score == null)
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
            score = calculateScore()
        } else if (movementResult.phraseChange) {
            state.enemyMoves()
            if (state.winningTeam != null || phraseLimit < state.phrase) {
                score = calculateScore()
            }
        }
    }

    private fun calculateScore() = (phraseLimit - state.phrase).toDouble() / phraseLimit * 0.3 +
            state.enemyDied.toDouble() / enemyCount * 0.7 - state.playerDied.toDouble() / playerCount * 0.2

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
            if (movementResult.phraseChange) {
                val enemyMoves = testState.enemyMoves()
                if (printMoves) {
                    enemyMoves.forEach(::println)
                }
            }
        }
        return testState
    }
}

data class FehMove(val unitAction: UnitAction) : Move