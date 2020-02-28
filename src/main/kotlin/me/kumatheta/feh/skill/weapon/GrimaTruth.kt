package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatEndSkill
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat
import me.kumatheta.feh.skill.aoeBuffAlly
import me.kumatheta.feh.skill.aoeDebuffFoe

object GrimaTruth : BasicWeapon(MagicR, 14, Stat(def = 3)) {
    override val combatEnd: CombatEndSkill? = { combatStatus, attacked ->
        if (attacked) {
            aoeDebuffFoe(combatStatus, Stat(atk = -5, spd = -5))
            aoeBuffAlly(combatStatus, Stat(atk = 5, spd = 5))
        }
    }
}