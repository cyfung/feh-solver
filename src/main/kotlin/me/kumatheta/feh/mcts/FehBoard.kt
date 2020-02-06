package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.MoveAndAttack
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

class FehBoard private constructor(
    private val phraseLimit: Int,
    state: BattleState,
    score: Long?,
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

    @Volatile
    override var score: Long? = score
        private set

    override fun copy(): FehBoard {
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
            } else if (!state.engaged && state.phrase > 6) {
                score = 0
            }
        }
    }

    fun calculateScore(battleState: BattleState) =
        battleState.enemyDied * 500L + (battleState.playerCount - battleState.playerDied) * 500L +
                battleState.unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 +  + battleState.unitsSeq(
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
            val copy = copy()
            copy.applyMove(it)
            copy.enemyCount < enemyCount
        }
    }
}

data class FehMove(val unitAction: UnitAction) : Move