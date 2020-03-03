package me.kumatheta.feh.mcts

import com.marcinmoskala.math.permutations
import me.kumatheta.feh.*
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh
import me.kumatheta.mcts.Board
import me.kumatheta.mcts.Move

data class FehBoardConfig(
    val phaseLimit: Int,
    val totalPlayerHp: Int,
    val maxTurnBeforeEngage: Int,
    val assistMap: Map<Int, Assist?>,
    val bossId: Int
)

interface FehBoard : Board<FehMove> {
    fun getStateCopy(): BattleState

    override val score: Long?
        get() = null
}

fun newFehBoard(
    phaseLimit: Int,
    state: BattleState,
    maxTurnBeforeEngage: Int,
    canRearrange: Boolean = true,
    bossId: Int = 5
): FehBoard {
    val totalPlayerHp = state.unitsSeq(Team.PLAYER).sumBy { it.maxHp }
    val assistMap = state.unitsSeq(Team.PLAYER).associate {
        it.id to it.assist
    }
    val config = FehBoardConfig(phaseLimit, totalPlayerHp, maxTurnBeforeEngage, assistMap, bossId)
    return if (canRearrange) {
        RearrangeFehBoard(config, state)
    } else {
        UnitActionTypeGrouping(config, state.copy(), state.getAllPlayerMovements())
    }
}

class RearrangeFehBoard(
    private val config: FehBoardConfig,
    private val state: BattleState
) : FehBoard {
    override val moves: List<FehMove> by lazy {
        (1..state.playerCount).toList().permutations().map {
            Rearrange(it)
        }
    }

    override fun getStateCopy() = state.copy()

    override fun applyMove(move: FehMove): Board<FehMove> {
        move as Rearrange
        val stateCopy = state.copy()
        stateCopy.rearrange(move.order)
        return UnitActionTypeGrouping(config, stateCopy, stateCopy.getAllPlayerMovements())
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

    override fun getStateCopy(): BattleState {
        throw UnsupportedOperationException()
    }

    override fun applyMove(move: FehMove): Board<FehMove> {
        throw UnsupportedOperationException()
    }

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        throw UnsupportedOperationException()
    }
}

abstract class Grouping<T>(
    val config: FehBoardConfig,
    private val state: BattleState,
    private val actions: Sequence<UnitAction>
) : FehBoard {
    override fun getStateCopy() = state.copy()
    private val actualMoves by lazy {
        actions.groupBy(this::selectKey)
    }

    abstract fun selectKey(unitAction: UnitAction): T

    abstract fun newBoard(
        config: FehBoardConfig,
        state: BattleState,
        actions: Sequence<UnitAction>,
        group: T
    ): FehBoard

    override val moves: List<FehMove> by lazy {
        actualMoves.keys.map { GroupMove(it) }
    }

    override fun applyMove(move: FehMove): Board<FehMove> {
        @Suppress("UNCHECKED_CAST")
        val group = (move as GroupMove<T>).value
        val actualMoves = actualMoves[group] ?: throw IllegalStateException()
        return newBoard(config, state, actualMoves.asSequence(), group)
    }

    final override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        val suggestedGroupOrder = suggestedGroupOrder
        @Suppress("UNCHECKED_CAST")
        nextMoves as List<GroupMove<T>>
        return if (suggestedGroupOrder != null) {
            val getValue: java.util.function.Function<GroupMove<T>, T> = java.util.function.Function { it.value }
            val comparator = Comparator.comparing<GroupMove<T>, T>(getValue, suggestedGroupOrder)
            nextMoves.asSequence().sortedWith(comparator)
        } else {
            nextMoves.asSequence()
        }
    }

    open val suggestedGroupOrder: Comparator<T>?
        get() = null

}

fun FehBoard.tryMoves(moves: List<FehMove>, printMoves: Boolean = false): BattleState {
    val currentState = getStateCopy()

    moves.forEach {
        when (it) {
            is Rearrange -> {
                currentState.rearrange((moves[0] as Rearrange).order)
            }
            is NormalMove -> {
                val unitAction = it.unitAction
                if (printMoves) {
                    println(unitAction)
                }
                val movementResult = currentState.playerMove(unitAction)
                when {
                    movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER -> {
                        // ignore
                    }
                    movementResult.phraseChange -> {
                        val enemyMoves = currentState.enemyMoves()
                        if (printMoves) {
                            enemyMoves.forEach(::println)
                        }
                    }
                }
            }
            else -> Unit
        }

    }
    return currentState
}

fun FehBoard.tryAndGetDetails(moves: List<FehMove>): List<Pair<UnitAction?, BattleState>> {
    val currentState = getStateCopy()
    return moves.asSequence().mapNotNull {
        when (it) {
            is Rearrange -> {
                currentState.rearrange((moves[0] as Rearrange).order)
                null
            }
            is NormalMove -> {
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
            }
            else -> null
        }
    }.flatMap { it }.toList()
}

class DangerLevelGrouping(
    config: FehBoardConfig,
    state: BattleState,
    actions: Sequence<UnitAction>,
    private val dangerAreas: Map<Position, Int>,
    private val bossDangerArea: Set<Position>
) : Grouping<Pair<Int, Boolean>>(config, state, actions) {
    override fun selectKey(unitAction: UnitAction): Pair<Int, Boolean> {
        return (dangerAreas[unitAction.moveTarget] ?: 0) to bossDangerArea.contains(unitAction.moveTarget)
    }

    override fun newBoard(
        config: FehBoardConfig,
        state: BattleState,
        actions: Sequence<UnitAction>,
        group: Pair<Int, Boolean>
    ): FehBoard {
        return StandardFehBoard(config, state, actions)
    }

    override val suggestedGroupOrder: Comparator<Pair<Int, Boolean>>? = compareBy({
        if (it.second) {
            1
        } else {
            0
        }
    }, {
        it.first
    })

}

class UnitActionTypeGrouping(
    config: FehBoardConfig,
    state: BattleState,
    actions: Sequence<UnitAction>
) : Grouping<Pair<Int, FehActionType>>(config, state, actions) {
    private val dangerAreas: Map<Position, Int>
    private val bossDangerArea: Set<Position>

    init {
        val stateDangerAreas = state.dangerAreas()
        dangerAreas =
            stateDangerAreas.values.asSequence().flatMap { it.keys.asSequence() }.groupingBy { it }.eachCount()
        bossDangerArea =
            stateDangerAreas.entries.firstOrNull { it.key.id == config.bossId }?.value?.keys?.toSet() ?: emptySet()
    }

    private fun Assist?.toActionType() = when (this) {
        is Refresh -> FehActionType.REFRESH
        is Heal -> FehActionType.HEAL
        else -> FehActionType.OTHERS
    }

    override fun newBoard(
        config: FehBoardConfig,
        state: BattleState,
        actions: Sequence<UnitAction>,
        group: Pair<Int, FehActionType>
    ): FehBoard {
        return DangerLevelGrouping(config, state, actions, dangerAreas, bossDangerArea)
    }

    override fun selectKey(unitAction: UnitAction): Pair<Int, FehActionType> {
        val actionType = when (unitAction) {
            is MoveAndAssist -> config.assistMap[unitAction.heroUnitId].toActionType()
            is MoveAndAttack -> FehActionType.ATTACK
            else -> FehActionType.OTHERS
        }
        return unitAction.heroUnitId to actionType
    }

    override val suggestedGroupOrder: Comparator<Pair<Int, FehActionType>>? = compareBy {
        when (it.second) {
            FehActionType.REFRESH -> 0
            FehActionType.ATTACK -> 1
            FehActionType.HEAL -> 1
            FehActionType.OTHERS -> 2
        }
    }


}

class StandardFehBoard(
    private val config: FehBoardConfig,
    private val state: BattleState,
    private val playerMoves: Sequence<UnitAction>
) : FehBoard {

    override val moves: List<FehMove> by lazy {
        playerMoves.map {
            NormalMove(it)
        }.toList()
    }

    override fun applyMove(move: FehMove): FehBoard {
        move as NormalMove
        check(score == null)
        val state = state.copy()
        val nextMove = move.unitAction
        val movementResult = state.playerMove(nextMove)
        val score = if (movementResult.gameEnd || movementResult.teamLostUnit == Team.PLAYER) {
            calculateScore(state)
        } else if (movementResult.phraseChange) {
            state.enemyMoves()
            if (state.playerDied > 0 || state.winningTeam != null || config.phaseLimit < state.phase) {
                calculateScore(state)
            } else if (!state.engaged && state.phase > config.maxTurnBeforeEngage * 2) {
                0L
            } else {
                null
            }
        } else {
            null
        }

        return if (score == null) {
            UnitActionTypeGrouping(config, state, state.getAllPlayerMovements())
        } else {
            ScoreFehBoard(score)
        }

    }

    private fun calculateScore(battleState: BattleState) =
        battleState.enemyDied * 500L + (battleState.playerCount - battleState.playerDied) * 500L +
                battleState.unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 + +battleState.unitsSeq(
            Team.ENEMY
        ).sumBy { it.maxHp - it.currentHp } * 2 + (config.phaseLimit - battleState.phase) * 20 + if (battleState.phase >= config.phaseLimit) {
            -1500L
        } else {
            0L
        }

    override fun getStateCopy() = state.copy()

    override fun suggestedOrder(nextMoves: List<FehMove>): Sequence<FehMove> {
        @Suppress("UNCHECKED_CAST")
        return nextMoves.asSequence()
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

enum class FehActionType {
    REFRESH,
    ATTACK,
    HEAL,
    OTHERS
}

data class GroupMove<T>(val value: T) : FehMove