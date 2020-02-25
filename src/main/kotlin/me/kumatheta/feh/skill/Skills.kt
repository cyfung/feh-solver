package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatStatus
import me.kumatheta.feh.InCombatStat
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