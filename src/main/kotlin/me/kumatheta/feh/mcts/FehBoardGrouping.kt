package me.kumatheta.feh.mcts

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh

abstract class Grouping<T>(
    move: FehMove?,
    config: FehBoardConfig,
    private val state: BattleState,
    private val actions: Sequence<UnitAction>
) : FehBoardWithState(move, state, config) {
    private val actualMoves by lazy {
        actions.groupBy(this::selectKey)
    }

    abstract fun selectKey(unitAction: UnitAction): T

    abstract fun newBoard(
        config: FehBoardConfig,
        state: BattleState,
        actions: Sequence<UnitAction>,
        group: T,
        move: GroupMove<T>
    ): FehBoard

    override val moves: List<FehMove> by lazy {
        actualMoves.keys.map { GroupMove(it) }
    }

    override fun applyMove(move: FehMove): FehBoard {
        @Suppress("UNCHECKED_CAST")
        val group = (move as GroupMove<T>).value
        val actualMoves = actualMoves[group] ?: throw IllegalStateException()
        return newBoard(config, state, actualMoves.asSequence(), group, move)
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

class DangerLevelGrouping(
    parent: FehBoardWithState?,
    move: FehMove?,
    config: FehBoardConfig,
    state: BattleState,
    actions: Sequence<UnitAction>,
    private val dangerAreas: Map<Position, Int>,
    private val bossDangerArea: Set<Position>
) : Grouping<Pair<Int, Boolean>>(move, config, state, actions) {
    override fun selectKey(unitAction: UnitAction): Pair<Int, Boolean> {
        return (dangerAreas[unitAction.moveTarget] ?: 0) to bossDangerArea.contains(unitAction.moveTarget)
    }

    override fun newBoard(
        config: FehBoardConfig,
        state: BattleState,
        actions: Sequence<UnitAction>,
        group: Pair<Int, Boolean>,
        move: GroupMove<Pair<Int, Boolean>>
    ): FehBoard {
        return StandardFehBoard(move, config, state, actions)
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
    parent: FehBoardWithState?,
    move: FehMove?,
    config: FehBoardConfig,
    state: BattleState,
    actions: Sequence<UnitAction>
) : Grouping<Pair<Int, FehActionType>>(move, config, state, actions) {
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
        group: Pair<Int, FehActionType>,
        move: GroupMove<Pair<Int, FehActionType>>
    ): FehBoard {
        return DangerLevelGrouping(
            this,
            move,
            config,
            state,
            actions,
            dangerAreas,
            bossDangerArea
        )
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