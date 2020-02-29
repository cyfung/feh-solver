package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe

fun smoke(debuff: Stat): CombatEndSkill = { combatStatus, _ ->
    if(!combatStatus.self.heroUnit.isDead) {
        combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team.foe).filter {
            it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2
        }.forEach {
            it.applyDebuff(debuff)
        }
    }
}
