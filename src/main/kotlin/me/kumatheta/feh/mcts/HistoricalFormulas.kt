package me.kumatheta.feh.mcts

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.MovementAssist
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh

fun BattleState.calculateScoreV1(config: FehBoardConfig): Long {
    return (config.phaseLimit - phase) * 200L / config.phaseLimit +
            enemyDied * 600L / enemyCount +
            (playerCount - playerDied) * 200L / playerCount
}

fun BattleState.calculateScoreV2(config: FehBoardConfig): Long {
    val phaseRemaining = config.phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 500L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 5 +
            unitsSeq(Team.ENEMY).sumBy { it.maxHp - it.currentHp } * 2 +
            phaseRemaining * 20
}

fun UnitAction.toRatingV1(config: FehBoardConfig): Int {
    return if (this is MoveAndAttack) {
        2
    } else {
        1
    }
}

fun UnitAction.toRatingV2(config: FehBoardConfig): Int {
    val assist = config.assistMap[heroUnitId]
    return if (assist is Refresh) {
        when (this) {
            is MoveAndAssist -> {
                5
            }
            else -> {
                0
            }
        }
    } else {
        when (this) {
            is MoveAndAttack -> 3
            is MoveAndAssist -> when (assist) {
                is Heal -> 3
                is MovementAssist -> 2
                else -> 1
            }
            else -> 1
        }
    }
}
