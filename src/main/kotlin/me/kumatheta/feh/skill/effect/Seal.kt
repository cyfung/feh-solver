package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.skill.CombatEndSkill
import me.kumatheta.feh.Stat

fun seal(stat: Stat): CombatEndSkill = { combatStatus, _ ->
    if (!combatStatus.self.heroUnit.isDead && !combatStatus.foe.heroUnit.isDead) {
        combatStatus.foe.heroUnit.applyDebuff(stat)
    }
}
