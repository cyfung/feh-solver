package me.kumatheta.feh.skill.weapon

import me.kumatheta.feh.BasicWeapon
import me.kumatheta.feh.CombatStartSkill
import me.kumatheta.feh.MagicR
import me.kumatheta.feh.Stat

private val IN_COMBAT_BUF = Stat(atk = 3, spd = 3)

object Gleipnir : BasicWeapon(MagicR, 14) {
    override val extraStat: Stat = Stat(res = 3)

    override val inCombatStat: CombatStartSkill<Stat>? = { combatStatus ->
        if(combatStatus.foe.currentHp == combatStatus.foe.maxHp) {
            IN_COMBAT_BUF
        } else {
            Stat.ZERO
        }
    }
}