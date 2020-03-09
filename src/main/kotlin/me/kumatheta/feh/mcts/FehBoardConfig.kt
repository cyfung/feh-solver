package me.kumatheta.feh.mcts

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.Team
import me.kumatheta.feh.skill.Assist

data class FehBoardConfig(
    val phaseLimit: Int,
    val totalPlayerHp: Int,
    val maxTurnBeforeEngage: Int,
    val assistMap: Map<Int, Assist?>,
    val bossId: Int,
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
            unitsSeq(Team.PLAYER).sumBy { it.currentHp } * 8 +
            unitsSeq(Team.ENEMY).sumBy { (it.maxHp - it.currentHp) * 270 / it.maxHp } +
            phaseRemaining * 20 +
            if (winningTeam == Team.PLAYER) {
                5000L
            } else {
                0L
            }

}