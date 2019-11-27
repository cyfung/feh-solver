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
    private val totalPlayerHp: Int
) : Board<FehMove> {
    private val state = state.copy()
    private val enemyCount: Int
        get() = state.enemyCount
    private val playerCount: Int
        get() = state.playerCount


    constructor(phraseLimit: Int, state: BattleState) : this(
        phraseLimit,
        state,
        null,
        state.unitsSeq(Team.PLAYER).sumBy { it.stat.hp }
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
        return FehBoard(phraseLimit, state, score, totalPlayerHp)
    }

    override fun applyMove(move: FehMove) {
        check(score == null)
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
            score = calculateScore(state)
        } else if (movementResult.phraseChange) {
            state.enemyMoves()
            if (state.playerDied > 0 || state.winningTeam != null || phraseLimit < state.phrase) {
                score = calculateScore(state)
            }
        }
    }

    fun calculateScore(battleState: BattleState) =
        battleState.enemyDied.toDouble() / enemyCount * 0.5 +
                0.1 - battleState.playerDied.toDouble() / playerCount * 0.1 +
                if (battleState.winningTeam == Team.PLAYER && battleState.playerDied == 0) {
                    0.1 + (phraseLimit - battleState.phrase).toDouble() / phraseLimit * 0.3
                } else {
                    0.0
                }

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
}

data class FehMove(val unitAction: UnitAction) : Move