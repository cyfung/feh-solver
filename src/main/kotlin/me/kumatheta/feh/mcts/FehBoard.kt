package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    state: BattleState,
    score: Double?,
    private val enemyCount: Int,
    private val playerCount: Int
) : Board<FehMove> {
    private val state = state.copy()

    constructor(phraseLimit: Int, state: BattleState) :
            this(
                phraseLimit,
                state,
                null,
                state.unitsSeq(Team.ENEMY).count(),
                state.unitsSeq(Team.PLAYER).count()
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
        return FehBoard(phraseLimit, state, score, enemyCount, playerCount)
    }

    val stateCopy
        get() = state.copy()

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

}

data class FehMove(val unitAction: UnitAction) : Move