package me.kumatheta.feh.skill

import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.Stat

fun seal(stat: Stat): CombatEndSkill = { combatStatus, _ ->
    if (!combatStatus.self.heroUnit.isDead && !combatStatus.foe.heroUnit.isDead) {
        combatStatus.foe.heroUnit.applyDebuff(stat)
    }
}
