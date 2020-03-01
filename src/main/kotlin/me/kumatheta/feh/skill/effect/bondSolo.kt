package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatStartSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.adjacentAllies

fun bondSolo(bond: Stat, solo: Stat): CombatStartSkill<Stat> {
    return { combatStatus ->
        val (battleState, self) = combatStatus
        if (self.adjacentAllies(battleState).any()) {
            bond
        } else {
            solo
        }
    }
}

fun bond(buff: Stat) = bondSolo(bond = buff, solo = Stat.ZERO)

fun solo(buff: Stat) = bondSolo(bond = Stat.ZERO, solo = buff)