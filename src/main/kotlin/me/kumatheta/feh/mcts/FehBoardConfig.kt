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
    val calculateScore: BattleState.(phaseLimit: Int) -> Long
)

fun BattleState.calculateTacticsDrillScore(phaseLimit: Int): Long {
    val phaseRemaining = phaseLimit - phase
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

fun BattleState.oldCalculateHeroBattleScore(phaseLimit: Int): Long {
    val phaseRemaining = phaseLimit - phase
    return enemyDied * 500L +
            (playerCount - playerDied) * 400L +
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 8 +
            unitsSeq(Team.ENEMY).sumBy { it.maxHp - it.currentHp } * 5 +
            phaseRemaining * 20 +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }

}

fun BattleState.calculateHeroBattleScore(phaseLimit: Int): Long {
    val phaseRemaining = phaseLimit - phase
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

fun BattleState.calculateScoreV1(phaseLimit: Int): Long {
    return (phaseLimit - phase) * 200L / phaseLimit +
            enemyDied * 600L / enemyCount +
            (playerCount - playerDied) * 200L / playerCount
}

fun UnitAction.toRatingAttackFirst(config: FehBoardConfig): Int {
    return if (this is MoveAndAttack) {
        2
    } else {
        1
    }
}

fun UnitAction.toRating(config: FehBoardConfig): Int {
    val assist = config.assistMap[heroUnitId]
    return if (assist is Refresh) {
        if (this is MoveAndAssist) {
            5
        } else {
            0
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