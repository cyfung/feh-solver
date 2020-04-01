package me.kumatheta.feh.skill.effect.others

import me.kumatheta.feh.skill.CombatStatus
import me.kumatheta.feh.skill.InCombatStat
import me.kumatheta.feh.skill.effect.PercentageDamageReduce
import me.kumatheta.feh.skill.inCombatVirtualSpd

val RepelEffect = object : PercentageDamageReduce {
    override fun getPercentageDamageReduce(combatStatus: CombatStatus<InCombatStat>, specialTriggered: Boolean): Int {
        val speedDiff = combatStatus.self.inCombatVirtualSpd - combatStatus.foe.inCombatVirtualSpd
        return if (speedDiff > 0) {
            minOf(speedDiff * 4, 40)
        } else {
            0
        }
    }
}