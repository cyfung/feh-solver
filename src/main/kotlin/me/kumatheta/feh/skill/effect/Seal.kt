package me.kumatheta.feh.skill.effect

import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.BasicSkill

fun seal(stat: Stat) = BasicSkill(
    combatEnd = { combatStatus, _ ->
        if (!combatStatus.self.heroUnit.isDead && !combatStatus.foe.heroUnit.isDead) {
            combatStatus.foe.heroUnit.applyDebuff(stat)
        }
    }
)
