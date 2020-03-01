package me.kumatheta.feh.skill

import me.kumatheta.feh.BattleState
import me.kumatheta.feh.HeroUnit
import me.kumatheta.feh.Stat

fun aoeDebuffFoe(
    combatStatus: CombatStatus<InCombatStat>,
    stat: Stat
) {
    combatStatus.battleState.unitsSeq(combatStatus.foe.heroUnit.team)
        .filter { it.position.distanceTo(combatStatus.foe.heroUnit.position) <= 2 }
        .forEach {
            it.applyDebuff(stat)
        }
}

fun aoeBuffAlly(
    combatStatus: CombatStatus<InCombatStat>,
    stat: Stat
) {
    combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team)
        .filter { it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2 }
        .forEach {
            it.applyBuff(stat)
        }
}

fun HeroUnit.adjacentAllies(
    battleState: BattleState
) = allies(battleState).filter { it.position.distanceTo(position) == 1 }

fun HeroUnit.nearbyAllies(
    battleState: BattleState,
    maxRange: Int
): Sequence<HeroUnit> {
    return allies(battleState).filter {
        it.position.distanceTo(position) <= maxRange
    }
}

fun HeroUnit.allies(battleState: BattleState) =
    battleState.unitsSeq(team).filterNot { it == this }

fun HeroUnit.inCardinalDirection(target: HeroUnit) =
    target.position.x == position.x || target.position.y == position.y

