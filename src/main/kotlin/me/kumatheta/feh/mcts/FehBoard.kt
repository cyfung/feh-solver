package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
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
                state.unitsAndPosSeq(Team.ENEMY).count(),
                state.unitsAndPosSeq(Team.PLAYER).count()
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
        val nextMove = move.unitAction
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
            (enemyCount - state.unitsAndPosSeq(Team.ENEMY).count()) / enemyCount * 0.6 +
            state.unitsAndPosSeq(Team.PLAYER).count() / playerCount * 0.2

}

data class FehMove(val unitAction: UnitAction) : Move