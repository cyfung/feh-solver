package com.bloombase.feh.mcts

import com.bloombase.feh.BattleState
import com.bloombase.feh.Team
import com.bloombase.feh.UnitMovement
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    private val state: BattleState,
    score: Double?,
    val enemyCount: Int,
    val playerCount: Int
) : Board<FehMove> {
    constructor(phraseLimit: Int, state: BattleState) :
            this(
                phraseLimit,
                state,
                null,
                state.unitsAndPos(Team.ENEMY).count(),
                state.unitsAndPos(Team.PLAYER).count()
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
        return FehBoard(phraseLimit, state.copy(), score, enemyCount, playerCount)
    }


    override fun applyMove(move: FehMove) {
        check(score == null)
        val nextMove = move.unitMovement
        when (state.playerMove(nextMove)) {
            BattleState.MovementResult.PLAYER_WIN -> {
                score = calculateScore()
            }
            BattleState.MovementResult.PLAYER_UNIT_DIED -> {
                score = calculateScore()
            }
            BattleState.MovementResult.PHRASE_CHANGE -> {
                state.enemyMoves()
                if (phraseLimit < state.phrase) {
                    score = calculateScore()
                }
            }
            BattleState.MovementResult.NOTHING -> Unit
        }
    }

    private fun calculateScore() = (phraseLimit - state.phrase).toDouble() / phraseLimit * 0.2 +
            (enemyCount - state.unitsAndPos(Team.ENEMY).count()) / enemyCount * 0.6 +
            state.unitsAndPos(Team.PLAYER).count() / playerCount * 0.2

}

data class FehMove(val unitMovement: UnitMovement) : Move