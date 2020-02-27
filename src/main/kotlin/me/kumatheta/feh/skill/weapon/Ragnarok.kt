package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat

private val IN_COMBAT_BUF = Stat(atk = 5, spd = 5)

object Ragnarok : BasicWeapon(MagicR, 14) {
    override val inCombatStat: CombatStartSkill<Stat>? = { combatStatus ->
        if (combatStatus.self.currentHp == combatStatus.self.maxHp) {
            IN_COMBAT_BUF
        } else {
            Stat.ZERO
        }
    }
}