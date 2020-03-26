package me.kumatheta.feh.mcts

import com.marcinmoskala.math.combinations
import com.marcinmoskala.math.permutations
import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Team
import me.kumatheta.feh.UnitAction
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move
import kotlin.random.Random

interface FehBoard : Board<FehMove> {
    override val score: Long?
        get() = null

    override fun applyMove(move: FehMove): FehBoard

    override fun getPlayOutMove(random: Random): FehMove {
        return moves.random(random)
    }
}

abstract class FehBoardWithState(
    private val move: FehMove?,
    private val state: BattleState,
    val config: FehBoardConfig
) : FehBoard {
    fun getStateCopy(): BattleState {
        return state.copy()
    }

    fun calculateScore(endState: BattleState, move: FehMove): Long {
        return config.calculateScore(endState, config)
    }

    final override fun applyMove(move: FehMove): FehBoard {
        return applyMove<Unit>(move, null).first
    }

    abstract fun <T : Any> applyMove(
        move: FehMove,
        stateListener: ((BattleState, UnitAction?) -> T)?
    ): Pair<FehBoard, Sequence<T>>
}

fun newFehBoard(
    phaseLimit: Int,
    maxTurnBeforeEngage: Int,
    canRearrange: Boolean = true,
    bossId: Int = 5,
    toRating: UnitAction.(config: FehBoardConfig) -> Int,
    calculateScore: BattleState.(config: FehBoardConfig) -> Long,
    stateBuilder: (List<HeroUnit>) -> BattleState,
    allPlayerUnits: List<HeroUnit>,
    playerCount: Int
): FehBoard {
    val availableUnitsCount = allPlayerUnits.size
    require(availableUnitsCount >= playerCount)
    if (availableUnitsCount == playerCount) {
        val internalState = stateBuilder(allPlayerUnits)
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
            toRating = toRating,
            calculateScore = calculateScore
        )

        return setupFirstState(null, canRearrange, config, internalState)
    }

    val config = FehBoardConfig(
        phaseLimit = phaseLimit,
        totalPlayerHp = 0,
        maxTurnBeforeEngage = maxTurnBeforeEngage,
        assistMap = emptyMap(),
        bossId = bossId,
        toRating = toRating,
        calculateScore = calculateScore
    )

    return TeamSelectFehBoard(
        stateBuilder = stateBuilder,
        config = config,
        allPlayerUnits = allPlayerUnits,
        playerCount = playerCount,
        canRearrange = canRearrange
    )

}

private fun setupFirstState(
    move: FehMove?,
    canRearrange: Boolean,
    config: FehBoardConfig,
    internalState: BattleState
): FehBoardWithState {
    return if (canRearrange) {
        RearrangeFehBoard(move, config, internalState)
    } else {
        internalState.rearrange((1..internalState.playerCount).toList())
        newInternalBoardChain(move, config, internalState)
    }
}

private fun newInternalBoardChain(
    move: FehMove?,
    config: FehBoardConfig,
    state: BattleState
): FehBoardWithState {
    return StandardFehBoard(move, config, state)
}

class TeamSelectFehBoard(
    private val stateBuilder: (List<HeroUnit>) -> BattleState,
    private val config: FehBoardConfig,
    private val allPlayerUnits: List<HeroUnit>,
    playerCount: Int,
    val canRearrange: Boolean
) : FehBoard {
    override val moves: List<FehMove> by lazy {
        allPlayerUnits.indices.toSet().combinations(playerCount).map {
            TeamSelect(it)
        }
    }

    override fun applyMove(move: FehMove): FehBoard {
        move as TeamSelect
        val playerUnits = move.team.map {
            allPlayerUnits[it]
        }
        val state = stateBuilder(playerUnits)
        val totalPlayerHp = state.unitsSeq(Team.PLAYER).sumBy { it.maxHp }
        val assistMap = state.unitsSeq(Team.PLAYER).associate {
            it.id to it.assist
        }
        return setupFirstState(
            move,
            canRearrange,
            config.copy(totalPlayerHp = totalPlayerHp, assistMap = assistMap),
            state
        )
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        return nextMoves.asSequence()
    }
}

class RearrangeFehBoard(
    move: FehMove?,
    config: FehBoardConfig,
    private val state: BattleState
) : FehBoardWithState(move, state, config) {
    override val moves: List<FehMove> by lazy {
        (1..state.playerCount).toList().permutations().map {
            Rearrange(it)
        }
    }

    override fun <T : Any> applyMove(
        move: FehMove,
        stateListener: ((BattleState, UnitAction?) -> T)?
    ): Pair<FehBoard, Sequence<T>> {
        move as Rearrange
        val stateCopy = getStateCopy()
        stateCopy.rearrange(move.order)
        val seq: Sequence<T> = if (stateListener == null) {
            emptySequence()
        } else {
            sequenceOf(stateListener.invoke(stateCopy, null))
        }
        return newInternalBoardChain(move, config, stateCopy) to seq
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

    override fun applyMove(move: FehMove): FehBoard {
        throw UnsupportedOperationException()
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        throw UnsupportedOperationException()
    }
}

fun FehBoard.tryAndGetDetails(moves: List<FehMove>): List<Pair<UnitAction?, BattleState>> {
    var board = this
    var initialized = false
    val seq = sequence {
        moves.asSequence().forEach {
            val boardWithState = board as? FehBoardWithState
            board = if (boardWithState != null) {
                if (!initialized) {
                    yield(null to boardWithState.getStateCopy())
                    initialized = true
                }
                val (newBoard, seq) = boardWithState.applyMove(it) { state, action ->
                    action to state.copy()
                }
                seq.forEach { pair ->
                    yield(pair)
                }
                newBoard
            } else {
                board.applyMove(it)
            }
        }
    }
    return seq.toList()
}

class StandardFehBoard(
    move: FehMove?,
    config: FehBoardConfig,
    private val state: BattleState,
    private val playerMoves: Sequence<UnitAction> = state.getAllPlayerMovements()
) : FehBoardWithState(move, state, config) {
    override val moves: List<FehMove> by lazy {
        playerMoves.map {
            NormalMove(it)
        }.toList()
    }

    override fun <T : Any> applyMove(
        move: FehMove,
        stateListener: ((BattleState, UnitAction?) -> T)?
    ): Pair<FehBoard, Sequence<T>> {
        move as NormalMove
        check(score == null)
        val state = getStateCopy()
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        var seq = if (stateListener == null) {
            emptySequence()
        } else {
            sequenceOf(stateListener.invoke(state, nextMove))
        }
        val score = if (config.phaseLimit < state.phase) {
            calculateScore(state, move)
        } else {
            if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
                calculateScore(state, move)
            } else if (movementResult.phraseChange) {
                if (stateListener == null) {
                    state.enemyMoves()
                } else {
                    seq += state.enemyMoves {
                        stateListener.invoke(state, it)
                    }.asSequence() + stateListener.invoke(state, null)
                }
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
        }

        return if (score == null) {
            newInternalBoardChain(move, config, state)
        } else {
            ScoreFehBoard(score)
        } to seq
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        @Suppress("UNCHECKED_CAST")
        nextMoves as List<NormalMove>
        return nextMoves.asSequence().sortedByDescending { move ->
            val unitAction = move.unitAction
            config.toRating(unitAction, config)
        }
    }

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

data class TeamSelect(val team: Set<Int>) : FehMove {
    override fun toString(): String {
        return "TeamSelect(setOf(${team.joinToString()}))"
    }
}

enum class FehActionType {
    REFRESH,
    ATTACK,
    HEAL,
    OTHERS
}

data class GroupMove<T>(val value: T) : FehMove