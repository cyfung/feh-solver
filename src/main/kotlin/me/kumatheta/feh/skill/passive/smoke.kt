package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.Stat
import me.kumatheta.feh.foe
import me.kumatheta.feh.skill.BasicSkill

fun smoke(debuff: Stat) = BasicSkill(combatEnd = { combatStatus, _ ->
    if (!combatStatus.self.heroUnit.isDead) {
        combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team.foe).filter {
            it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2
        }.forEach {
            it.applyDebuff(debuff)
        }
    }
})

val pulseSmoke3 = BasicSkill(
    combatEnd = { combatStatus, _ ->
        if (!combatStatus.self.heroUnit.isDead) {
            combatStatus.battleState.unitsSeq(combatStatus.self.heroUnit.team.foe).filter {
                it.position.distanceTo(combatStatus.self.heroUnit.position) <= 2
            }.forEach {
                it.cachedEffect.cooldown++
            }
        }
    }
)
