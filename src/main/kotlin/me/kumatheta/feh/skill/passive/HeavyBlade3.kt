package me.kumatheta.feh.skill.passive

import me.kumatheta.feh.skill.BasicSkill
import me.kumatheta.feh.skill.CooldownChange
import me.kumatheta.feh.skill.InCombatSkill
import me.kumatheta.feh.skill.Passive

object HeavyBlade3 : BasicSkill() {
    override val cooldownBuff: InCombatSkill<CooldownChange<Int>>? = {
        if (it.self.inCombatStat.atk > it.foe.inCombatStat.atk) {
            CooldownChange(1, 0)
        } else {
            CooldownChange(0, 0)
        }
    }
}