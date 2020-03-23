package me.kumatheta.feh.mcts

import me.kumatheta.feh.*
import me.kumatheta.feh.skill.Assist
import me.kumatheta.feh.skill.MovementAssist
import me.kumatheta.feh.skill.assist.Heal
import me.kumatheta.feh.skill.assist.Refresh

data class FehBoardConfig(
    val phaseLimit: Int,
    val totalPlayerHp: Int,
    val maxTurnBeforeEngage: Int,
    val assistMap: Map<Int, Assist?>,
    val bossId: Int,
    val toRating: UnitAction.(config: FehBoardConfig) -> Int,
    val calculateScore: BattleState.(config: FehBoardConfig) -> Long
)

fun BattleState.calculateTacticsDrillScore(config: FehBoardConfig): Long {
    val phaseRemaining = config.phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 400L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 2 +
            unitsSeq(Team.ENEMY).sumBy { it.maxHp - it.currentHp } * 2 +
            phaseRemaining * 20 +
            if (phaseRemaining < 0) {
                0L
            } else {
                1300L
            } +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }


}

fun BattleState.oldCalculateHeroBattleScore(config: FehBoardConfig): Long {
    val phaseRemaining = config.phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 600L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 4 +
            unitsSeq(Team.ENEMY).sumBy { it.maxHp - it.currentHp } * 5 +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }

}

fun BattleState.calculateHeroBattleScore(config: FehBoardConfig): Long {
    val phaseRemaining = config.phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 400L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 7 +
            unitsSeq(Team.ENEMY).sumBy { (it.maxHp - it.currentHp) * 320 / it.maxHp } +
            phaseRemaining * 20 +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }

}

fun UnitAction.toRating(config: FehBoardConfig): Int {
    val assist = config.assistMap[heroUnitId]
    return if (assist is Refresh) {
        when (this) {
            is MoveAndAssist -> {
                3
            }
            is MoveAndAttack -> {
                2
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

fun BattleState.toScore(config: FehBoardConfig): Long {
    val phaseRemaining = config.phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 400L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 3 +
            unitsSeq(Team.ENEMY).sumBy { (it.maxHp - it.currentHp) * 150 / it.maxHp } +
            phaseRemaining * 40 +
            if (unitsSeq(Team.ENEMY).any { it.id == config.bossId }) {
                0
            } else {
                300
            } +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }
}