package me.kumatheta.feh.mcts

import me.kumatheta.feh.*

fun BattleState.calculateScoreV1(config: FehBoardConfig): Long {
    return (config.hardPhaseLimit - phase) * 200L / config.hardPhaseLimit +
            enemyDied * 600L / enemyCount +
            (playerCount - playerDied) * 200L / playerCount
}

fun BattleState.calculateScoreV2(config: FehBoardConfig): Long {
    val phaseRemaining = config.hardPhaseLimit - phase
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